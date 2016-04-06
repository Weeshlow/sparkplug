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

package software.uncharted.sparkplug

import com.typesafe.config.ConfigFactory
import org.apache.spark.{SparkConf, SparkContext}
import software.uncharted.sparkplug.handler.PlugHandler
import software.uncharted.sparkplug.listener.PlugListener

class Plug {
  val handlers = collection.mutable.Map[String, PlugHandler]()

  private val config = ConfigFactory.load()
  private val master = config.getString("sparkplug.master")

  private val conf = new SparkConf().setAppName("sparkplug").setMaster(master)
  val sc: SparkContext = new SparkContext(conf)

  println("Connected to Spark.")

  println("Connecting to RabbitMQ.")
  val listener: PlugListener = PlugListener.getInstance()
  listener.connect()

  def run(): Unit = {
    println("Kicking off consume.")
    listener.consume()

    println("Kicked off consume.")
  }

  def shutdown(): Unit = {
    Console.out.println("Shutting down.")
    listener.end()
  }

  def registerHandler(command: String, handler: PlugHandler) : Unit = {
    handlers.put(command, handler)
  }

  def unregisterHandler(command: String) : Unit = {
    handlers.remove(command)
  }
}
