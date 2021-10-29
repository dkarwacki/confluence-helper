# App for deleting child pages on Confluence

How to build and run the app:

1) `sbt assembly`
2) `cd target/scala-2.13/`
3) `java -DURL={confluence-url} -DAPI_KEY={confluence-api-key} -DEMAIL={confluence-user-email} -jar confluence-helper.jar {parent-page-id}`

   Example: `java -DURL=https://confluence-url.net -DAPI_KEY=abC13455Abcd9876aB -DEMAIL=john.doe@gmail.com -jar confluence-helper.jar 123456789`
