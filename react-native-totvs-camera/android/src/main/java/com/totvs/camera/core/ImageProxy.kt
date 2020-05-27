package com.totvs.camera.core

public interface ImageProxy : AutoCloseable {
    override fun close()
}