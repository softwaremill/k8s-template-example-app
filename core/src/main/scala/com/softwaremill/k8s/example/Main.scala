package com.softwaremill.k8s.example

import java.io.StringWriter
import java.util.concurrent.atomic.AtomicBoolean

import cats.effect._
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import io.prometheus.client.{CollectorRegistry, Counter}
import io.prometheus.client.exporter.common.TextFormat
import io.prometheus.client.hotspot.DefaultExports
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze._

object Main extends IOApp with StrictLogging {

  // metrics
  DefaultExports.initialize()
  val requestCounter: Counter = Counter.build().name("request_count").help("The request count").register()

  val isAlive = new AtomicBoolean(true)

  val helloWorldService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "hello" / name =>
      if (name.equalsIgnoreCase("adam")) {
        logger.error("Security alert!", new IllegalStateException())
      } else {
        logger.info(s"Saying hello to $name.")
      }
      requestCounter.inc()
      Ok(s"Hello, $name.")

    case POST -> Root / "kill" =>
      logger.info("Killing the service")
      isAlive.set(false)
      Ok()
  }

  val metricsService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      val writer = new StringWriter
      TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples)
      Ok(writer.toString)
  }

  val statusService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root =>
      if (isAlive.get()) {
        Ok()
      } else {
        ServiceUnavailable()
      }
  }

  val httpApp: HttpApp[IO] = Router("/api" -> helloWorldService, "/metrics" -> metricsService, "/status" -> statusService).orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .withHttpApp(httpApp)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}
