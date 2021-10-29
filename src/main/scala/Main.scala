import cats.effect.{ExitCode, IO, IOApp}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.log4cats.Logger
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    implicit val logger: Logger[IO] = Slf4jLogger.getLogger[IO]
    val config = ConfigSource.default.loadOrThrow[Configuration]

    val pageIdOpt = args.headOption.flatMap(_.toLongOption)

    pageIdOpt match {
      case Some(pageId) =>
        AsyncHttpClientCatsBackend
          .resource[IO]()
          .use { implicit backend =>
            val confluenceApiClient =
              new ConfluenceApiClientImpl(config.confluence)
            val program = for {
              childPage <- fs2.Stream.evals(
                confluenceApiClient.findAllChildPages(pageId)
              )
              _ <- fs2.Stream.eval(confluenceApiClient.removePage(childPage.id))
            } yield ()

            for {
              _ <- logger.info(s"URL: ${config.confluence.url}")
              _ <- logger.info(s"EMAIL: ${config.confluence.email}")
              _ <- logger.info(s"PARENT PAGE ID: $pageId")
              _ <- program.compile.drain
            } yield ()
          }
          .as(ExitCode.Success)
      case None =>
        logger
          .error("You must provide page id as an argument")
          .as(ExitCode.Error)
    }
  }
}
