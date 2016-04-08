/*
 * Copyright 2015-2016 Uncharted Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package software.uncharted.sparkplug.listener

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl.Source
import io.scalac.amqp.Connection
import org.apache.spark.SparkContext
import software.uncharted.sparkplug.handler.PlugHandler
import software.uncharted.sparkplug.model.PlugMessage

class PlugListener private(sparkContext: SparkContext) {
  private val handlers = collection.mutable.Map[String, PlugHandler]()

  private var connection: Option[Connection] = None
  private var connected: Boolean = false

  private implicit val system = ActorSystem("SparkPlug")
  private implicit val materializer = ActorMaterializer()

  @throws(classOf[PlugListenerException])
  def connect(): PlugListener = {
    if (!connected) {
      try {
        connection = Some(Connection())
        connected = true
      } catch {
        case e: Exception =>
          Console.err.println(s"Could not connect to RabbitMQ: $e")
          throw new PlugListenerException("Could not connect to RabbitMQ.", e)
      }
    }
    this
  }

  def shutdown(): PlugListener = {
    Console.out.println(s"Checking if connected: $connected; disconnecting if we are.")

    if (connected) {
      Console.out.println("Shutting down PlugListener.")
      try {
        materializer.shutdown()
        system.shutdown()
        connection.get.shutdown()
        Console.out.println("PlugListener shutdown.")
      } catch {
        case e: Exception =>
          Console.err.println(s"Could not disconnect from RabbitMQ: $e")
      }
    }
    this
  }

  def registerHandler(command: String, handler: PlugHandler) : Unit = {
    handlers.put(command, handler)
  }

  def unregisterHandler(command: String) : Unit = {
    handlers.remove(command)
  }

  def run(): Unit = {
    Console.out.println("Consuming.")
    val size: Int = 500
    Source.fromPublisher(connection.get.consume("q_sparkplug"))
      .buffer(size, OverflowStrategy.backpressure)
      .runForeach(p => {
        val message = PlugMessage.fromMessage(p.message)
        val handler = handlers.get(message.command)
        if (handler.isEmpty) {
          throw new PlugListenerException(s"No handler specified for command $message.command")
        }

        handler.get.onMessage(sparkContext, message)
      })
    Console.out.println("Consumption completed.")
  }

  def isConnected: Boolean = {
    connected
  }

  def getConnection: Connection = {
    connection.get
  }
}

object PlugListener {
  private var instance: Option[PlugListener] = None

  def getInstance(sparkContext: SparkContext): PlugListener = {
    if (instance.isEmpty) {
      instance = Some(new PlugListener(sparkContext))
    }
    instance.get
  }
}
