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

import org.scalatest.tools.Runner

object SparkplugInstance {
  val sparkplug = new Sparkplug()
}

object Main {
  def main(args: Array[String]): Unit = {
    if (sys.env.isDefinedAt("SPARK_JAVA_OPTS") && sys.env("SPARK_JAVA_OPTS").contains("jdwp=transport=dt_socket")) {
      print("Sleeping for 8 seconds. Please connect your debugger now...")
      val l: Long = 8000
      Thread.sleep(l)
    }

    val testResult = Runner.run(Array("-o", "-R", "build/classes/test"))
    if (!testResult) {
      System.exit(1)
    }
  }
}
