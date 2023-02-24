package th.co.dv.p2p.common.utilities

object LoggerUtils {
    fun START_STAGE(message: String) = "========== Starting: $message =========="
    fun END_STAGE(message: String) = "========== Ending: $message =========="
    const val LOG_STREAMING_SERVER_COMPLETE = "StreamingService: streaming server completed."
}