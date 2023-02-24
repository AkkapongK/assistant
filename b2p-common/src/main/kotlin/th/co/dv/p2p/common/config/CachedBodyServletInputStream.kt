package th.co.dv.p2p.common.config

import java.io.ByteArrayInputStream
import java.io.InputStream
import javax.servlet.ReadListener

import javax.servlet.ServletInputStream

class CachedBodyServletInputStream(cachedBody: ByteArray) : ServletInputStream() {
    private val cachedBodyInputStream: InputStream

    init {
        cachedBodyInputStream = ByteArrayInputStream(cachedBody)
    }

    override fun read(): Int {
        return cachedBodyInputStream.read()
    }

    override fun isFinished(): Boolean {
        return cachedBodyInputStream.available() == 0
    }

    override fun isReady(): Boolean {
        return true
    }

    override fun setReadListener(listener: ReadListener?) {
        throw UnsupportedOperationException()
    }

}