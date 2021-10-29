import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import org.typelevel.log4cats.Logger
import sttp.client3.{basicRequest, ResponseException, SttpBackend, UriContext}
import sttp.client3.circe.asJson
import sttp.model.Header

import java.nio.charset.StandardCharsets
import java.util.Base64

trait ConfluenceApiClient {
  def findAllChildPages(pageId: Long): IO[List[ChildPage]]
  def removePage(pageId: Long): IO[Unit]
}

class ConfluenceApiClientImpl(confluenceConfig: ConfluenceConfiguration)(
    implicit
    backend: SttpBackend[IO, Any],
    logger: Logger[IO]
) extends ConfluenceApiClient {

  private lazy val credentials = Base64.getEncoder.encodeToString(
    s"${confluenceConfig.email}:${confluenceConfig.apiKey}"
      .getBytes(StandardCharsets.UTF_8)
  )

  def findAllChildPages(pageId: Long): IO[List[ChildPage]] = {
    val stream = for {
      childPages <- fs2.Stream.unfoldLoopEval(findChildPages(pageId)) {
        response =>
          response.map(childPages => {
            val nextCallOpt = (for {
              childPage <- fs2.Stream.emits(childPages)
              nestedChildPage <- fs2.Stream.evals(
                findAllChildPages(childPage.id)
              )
            } yield nestedChildPage).compile.toList.some

            if (childPages.isEmpty)
              (childPages, None)
            else
              (childPages, nextCallOpt)
          })
      }
      childPage <- fs2.Stream.emits(childPages)
    } yield childPage
    stream.compile.toList
  }

  private def findChildPages(pageId: Long): IO[List[ChildPage]] = {
    val stream = for {
      childPages <- fs2.Stream
        .unfoldLoopEval(findChildPages(pageId, start = 0)) { pagedResponse =>
          pagedResponse.flatMap { case (childPages, lastStart) =>
            childPages match {
              case Right(childPages) =>
                val nextCallOpt =
                  Option.when(childPages.size >= childPages.limit)(
                    findChildPages(pageId, lastStart + childPages.size)
                  )
                IO.pure(childPages.results, nextCallOpt)
              case Left(error) =>
                logger.error(
                  s"Cannot find child pages for pageId: $pageId. Reason: ${error.getMessage}"
                ) *> IO.pure(List.empty, None)
            }
          }
        }
      childPage <- fs2.Stream.emits(childPages)
    } yield childPage
    stream.compile.toList
  }

  private def findChildPages(
      pageId: Long,
      start: Int
  ): IO[(Either[ResponseException[String, io.circe.Error], ChildPages], Int)] =
    basicRequest
      .get(
        uri"${confluenceConfig.url}/wiki/rest/api/content/$pageId/child/page?start=$start"
      )
      .header(
        Header.authorization("Basic", credentials)
      )
      .response(asJson[ChildPages])
      .send(backend)
      .map(r => (r.body, start))

  override def removePage(pageId: Long): IO[Unit] = for {
    result <- basicRequest
      .delete(
        uri"${confluenceConfig.url}/wiki/rest/api/content/$pageId"
      )
      .header(
        Header.authorization("Basic", credentials)
      )
      .send(backend)
      .map(r => r.body)
    _ <- result match {
      case Left(error) =>
        logger.error(s"Cannot delete page with id: $pageId. Reason: $error")
      case Right(_) =>
        logger.info(s"Successfully deleted page with id: $pageId")
    }
  } yield ()
}
