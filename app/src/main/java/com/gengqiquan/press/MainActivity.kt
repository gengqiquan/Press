package com.gengqiquan.press

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.SeekBar
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.gengqiquan.result.RxActivityResult
import com.xhe.photoalbum.PhotoAlbum
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File
import java.io.FileInputStream
import java.math.BigDecimal


class MainActivity : AppCompatActivity() {
    var path = ""
    var bitmap: Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_chose.setOnClickListener {
            RxActivityResult.with(this).
                    startActivityWithResult(PhotoAlbum(this)
                            .albumIntent)
                    .map { PhotoAlbum.parseResult(it) }
                    .subscribe({
                        sb_w.progress = 100
                        sb_h.progress = 100
                        path = it[0]
                        Glide.with(this).load(it[0]).asBitmap().into(object : SimpleTarget<Bitmap>() {
                            override fun onResourceReady(b: Bitmap?, glideAnimation: GlideAnimation<in Bitmap>?) {
                                iv_img.setImageBitmap(b)
                                bitmap = b
                                tv_info.text = "图片宽度为" + bitmap?.width + "px，高度为" + bitmap?.height + "px"
                            }
                        })
                        tv_size.text = "文件大小为：" + getFileSize(it[0]) + "KB"
                    }) { it.printStackTrace() }
        }
        sb_w.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.e("progress", progress.toString())


            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                showSize()
            }
        })
        sb_h.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.e("progress", progress.toString())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                showSize()
            }
        })
        tv_preview.setOnClickListener {
            val scale = sb_w.progress / 100f
            val quality = sb_h.progress
            val p = ImageUtil.compressImg(this@MainActivity, path, scale, quality)
            Glide.with(this).load(p).into(iv_img)
        }
        tv_sure.setOnClickListener {
            val scale = sb_w.progress / 100f
            val quality = sb_h.progress
            val p = ImageUtil.compressImg(this@MainActivity, path, scale, quality)
            share(p)
        }
    }

    fun showSize() {
        val scale = sb_w.progress / 100f
        val quality = sb_h.progress
        if (bitmap != null && !bitmap!!.isRecycled) {
            val w = bitmap!!.width * scale
            val h = bitmap!!.height * scale
            tv_info.text = "图片宽度为" + w.toInt() + "px，高度为" + h.toInt() + "px"
        }
        rx.Observable.create(object : Observable.OnSubscribe<Float> {
            override fun call(t: Subscriber<in Float>?) {

                val p = ImageUtil.compressImg(this@MainActivity, path, scale, quality)

                t?.onNext(getFileSize(p))
                t?.onCompleted()
            }
        }).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tv_size.text = "文件大小为：" + it.toString() + "KB"
                }) { it.printStackTrace() }

    }

    fun share(path: String) {
        val intent2 = Intent(Intent.ACTION_SEND);
        val uri = Uri.fromFile(File(path));
        intent2.putExtra(Intent.EXTRA_STREAM, uri);
        intent2.setType("image/*");
        startActivity(Intent.createChooser(intent2, "share"));
    }

    @Throws(Exception::class)
    fun getFileSize(path: String): Float {
        val file = File(path)
        if (file!!.exists()) {
            var fis = FileInputStream(file)
            val size = fis!!.available() / 1024f
            val b = BigDecimal(size.toDouble())
            val f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toFloat()
            return f1
        }
        return 0f
    }
}
