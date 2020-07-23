package com.github.kright.editor

import com.github.kright.interpreter.Output
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


class InterpreterWrapper {
    private val isFinished = AtomicBoolean(false)

    fun stop() {
        isFinished.set(true)
    }

    val wasStopped: Boolean
        get() = isFinished.get()

    companion object {
        fun runCode(
            interpreterPath: File,
            code: String,
            output: Output,
            stopCallback: (InterpreterWrapper) -> Unit
        ): InterpreterWrapper {
            val result = InterpreterWrapper()
            thread {
                val process = try {
                    ProcessBuilder(interpreterPath.absolutePath, "-c" ,code).start()
                } catch (ex: IOException) {
                    output.error(ex.message ?: "")
                    result.stop()
                    stopCallback(result)
                    return@thread
                }
                output.error("run ${code}")

                val inputStream = BufferedReader(InputStreamReader(process.inputStream))
                val errorStream = BufferedReader(InputStreamReader(process.errorStream))

                while (true) {
                    if (result.wasStopped) {
                        process.destroyForcibly()
                        output.error("process was terminated")
                        break
                    }
                    if (errorStream.ready()) {
                        output.error(errorStream.readLine())
                        continue
                    }
                    if (inputStream.ready()) {
                        output.print(inputStream.readLine())
                        continue
                    }
                    if (!process.isAlive) {
                        result.stop()
                        output.error("process finished with code ${process.exitValue()}")
                        break
                    }
                    Thread.sleep(1)
                }
                stopCallback(result)
            }

            return result
        }
    }
}
