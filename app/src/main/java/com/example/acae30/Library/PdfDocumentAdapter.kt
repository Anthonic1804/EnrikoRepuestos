package com.example.acae30.Library

import android.content.Context
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class PdfDocumentAdapter(context: Context, path:String) : PrintDocumentAdapter() {

    internal var context: Context?= null
    internal var path = ""

    init {
        this.context = context;
        this.path = path;
    }
    override fun onLayout(
        p0: PrintAttributes?,
        p1: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        layoutResultCallback: LayoutResultCallback?,
        p4: Bundle?
    ) {
        if(cancellationSignal!!.isCanceled){
            layoutResultCallback!!.onLayoutCancelled()
        }else{
            val builder = PrintDocumentInfo.Builder("Documento")
            builder.setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .build()

            layoutResultCallback!!.onLayoutFinished(builder.build(), true)
        }
    }

    override fun onWrite(
        pageRange: Array<out PageRange>?,
        parcelFileDescriptor: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        writeResultCallback: WriteResultCallback?
    ) {
        var `in`: InputStream ?= null
        var out : OutputStream ?= null

        try {
            val file = File(path)
            `in` = FileInputStream(file)
            out = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

            if(!cancellationSignal!!.isCanceled){
                `in`.copyTo(out)
                writeResultCallback!!.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
            }else{
                writeResultCallback!!.onWriteCancelled()
            }
        }catch (e: Exception){
            writeResultCallback!!.onWriteFailed(e.message)
            e.message?.let { Log.e("MENSAJE", it) }
        }finally {
            try {
                `in`!!.close()
                out!!.close()
            }catch (e: IOException){
                e.message?.let { Log.e("MENSAJE", it) }
            }
        }
    }

}