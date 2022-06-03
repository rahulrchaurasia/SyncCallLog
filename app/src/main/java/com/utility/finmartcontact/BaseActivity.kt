package com.utility.finmartcontact

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.utility.finmartcontact.core.controller.facade.ApplicationPersistance
import java.io.File
import java.io.FileOutputStream


open class BaseActivity : AppCompatActivity() {

    lateinit var dialog: AlertDialog
    lateinit var dialogView: View
    lateinit var applicationPersistance: ApplicationPersistance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDialog()
        applicationPersistance = ApplicationPersistance(this)
    }

    protected fun showMessage(
        view: View,
        message: String,
        action: String,
        onClickListener: View.OnClickListener?
    ) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAction(action, onClickListener).show()
    }

    //region Progress Dialog

    private fun initDialog() {
        val builder = AlertDialog.Builder(this)
        dialogView = layoutInflater.inflate(R.layout.layout_progress_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
    }

    fun showLoading(message: CharSequence) {
        val msg = dialogView.findViewById<TextView>(R.id.txtProgressTitle)
        msg.text = message
        dialog.show()
    }

    fun dismissDialog() {
        if (dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
    }

    open fun createFile(name: String): File? {
        val outStream: FileOutputStream? = null
        val dir: File = createDirIfNotExists()
        var fileName = "$name.doc"
        fileName = fileName.replace("\\s+".toRegex(), "")
        return File(dir, fileName)
    }

    open fun stringtopdf(strData: String):  File? {
        val outStream: FileOutputStream? = null


        val dir: File = createDirIfNotExists()
        val file: File = File(dir, "sample.pdf")
        file.createNewFile();
        val fOut: FileOutputStream = FileOutputStream(file)

        val document = PdfDocument()
        val pageInfo = PageInfo.Builder(100, 100, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()

        canvas.drawText(strData, 10F, 10F, paint)



        document.finishPage(page)
        document.writeTo(fOut)
        document.close()

        return  file
    }

    open fun sharePdfFile(pdfFileName: String?, urlToShare: String?, uri: Uri?) {
        try {
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "application/pdf"
            // share.setType("text/plain");
            share.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            share.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            share.setDataAndType(uri, uri?.let { contentResolver.getType(it) })
            share.putExtra(Intent.EXTRA_TEXT, urlToShare)
            share.putExtra(Intent.EXTRA_SUBJECT, "Quote")
            share.putExtra(Intent.EXTRA_STREAM, uri)
            val intent = Intent.createChooser(share, "Share Quote")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun createDirIfNotExists(): File {
        val file = File(Environment.getExternalStorageDirectory(), "/SynContact")
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("TravellerLog :: ", "Problem creating Image folder")
            }
        }
        return file
    }

    //endregion


    protected fun hideKeyBoard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }






}