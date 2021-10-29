case class ConfluenceConfiguration(
    url: String,
    apiKey: String,
    email: String
)
case class Configuration(confluence: ConfluenceConfiguration)
