package jmh

import java.io.InputStream

import akka.util.ByteString
import benchmark.grpc.GetEmployeeRequest
import com.google.protobuf.CodedInputStream
import org.openjdk.jmh.annotations.{Scope, _}

@State(Scope.Thread)
class MyState {
  val req: GetEmployeeRequest = GetEmployeeRequest(42)
  val reqAsBytes: Array[Byte] = req.toByteArray

  val inStream: InputStream = ByteString(reqAsBytes).iterator.asInputStream

  // This buffer will be used to create `CodedInputStream`
  val sharedBuffer: Array[Byte] = Array.ofDim[Byte](4*1024)
}

class GeneratedMessageCompanionBenchmark {
  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  def doNotReuseCodedInputStream(state: MyState): GetEmployeeRequest = {
    GetEmployeeRequest.parseFrom(state.inStream)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.Throughput))
  def reuseCodedInputStream(state: MyState): GetEmployeeRequest = {
    System.arraycopy(state.reqAsBytes, 0, state.sharedBuffer, 0, state.reqAsBytes.length)
    val stream: CodedInputStream = CodedInputStream.newInstance(state.sharedBuffer, 0, state.reqAsBytes.length)
    GetEmployeeRequest.parseFrom(stream)
  }
}