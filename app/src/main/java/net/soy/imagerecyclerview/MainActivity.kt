package net.soy.imagerecyclerview

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        var AUTHORITY: String = BuildConfig.APPLICATION_ID + ".provider"

        const val PERMISSIONS_REQUEST_READ_EXT_STORAGE = 1000
        const val PERMISSIONS_REQUEST_CAMERA = 1001

        const val REQUEST_TAKE_PICTURE = 2000
        const val REQUEST_PICK_IMAGE = 2001
    }

    private var horizontalLayoutManager: LinearLayoutManager? = null
    private var verticalLayoutManager: LinearLayoutManager? = null
    var horizontalImageAdapter: HorizontalImageAdapter? = null
    var verticalImageAdapter: VerticalImageAdapter? = null
    var mIsHorizontal: Boolean? = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        horizontalImageAdapter = HorizontalImageAdapter(this, applicationContext)
        verticalImageAdapter = VerticalImageAdapter(this, applicationContext)
        horizontalLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        verticalLayoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)

        rv_horizontal.layoutManager = horizontalLayoutManager
        rv_horizontal.adapter = horizontalImageAdapter

        rv_vertical.layoutManager = verticalLayoutManager
        rv_vertical.adapter = verticalImageAdapter

        horizontalImageAdapter?.init()
        verticalImageAdapter?.init()
    }

    /**
     * 갤러리, 사진촬영 선택 alert
     */
    fun showSelectAlert() {
        Log.w(TAG, "showSelectAlert()")
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("갤러리 또는 사진을 선택해주세요. ")
        alertDialog.setNegativeButton("사진") { _, _ -> requestCameraPermission() }
        alertDialog.setPositiveButton("갤러리") { _, _ -> requestReadExternalStoragePermission() }
        alertDialog.show()
    }

    /**
     * 갤러리 접근 권한 확인 메소드
     */
    private fun requestReadExternalStoragePermission() {
        Log.d(TAG, "requestReadExternalStoragePermission()")
        if (applicationContext != null) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext!!,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "#### READ_EXTERNAL_STORAGE, Permission 1")
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    Log.e(TAG, "#### READ_EXTERNAL_STORAGE, Permission 2")
                    showPopupForPermission()
                } else {
                    Log.e(TAG, "#### READ_EXTERNAL_STORAGE, Permission 3")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                            PERMISSIONS_REQUEST_READ_EXT_STORAGE
                        )
                    }
                }
            } else {
                Log.e(TAG, "#### READ_EXTERNAL_STORAGE, Permission 4")
                pickImage()
            }
        }
    }

    /**
     * 겔러리 호출 메소드
     */
    private fun pickImage() {
        Log.w(TAG, "pickImage()")
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"

        //단일 선택시
        //startActivityForResult(intent, REQUEST_PICK_IMAGE)

        //다중 선택
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_PICK_IMAGE)
    }

    /**
     * 카메라 권한 확인 메소드
     */
    private fun requestCameraPermission() {
        Log.w(TAG, "requestCameraPermission()")
        if (applicationContext != null) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext!!,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "#### CAMERA, Permission 1")
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {
                    Log.e(TAG, "#### CAMERA, Permission 2")
                    showPopupForPermission()
                } else {
                    Log.e(TAG, "#### CAMERA, Permission 3")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(
                            arrayOf(Manifest.permission.CAMERA),
                            PERMISSIONS_REQUEST_CAMERA
                        )
                    }
                }
            } else {
                Log.e(TAG, "#### CAMERA, Permission 4")
                takePicture()
            }
        }
    }

    /**
     * 사진 권한 허용 alert
     */
    private fun showPopupForPermission() {
        Log.w(TAG, "showPopupForPermission()")
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setMessage("사진/갤러리 접근권한을 허용해야 합니다")
        alertDialog.setPositiveButton("권한 설정") { _, _ -> goSettingPermission() }
        alertDialog.setNegativeButton("취소") { _, _ -> }
        alertDialog.show()
    }

    /**
     * 이미지를 임시 저장할 파일 생성
     */
    private var imageUri: Uri? = null
    var file: File? = null

    private fun createImageFile(): File? {
        val storageDir = applicationContext?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        try {
            file = File.createTempFile(timeStamp, ".jpg", storageDir)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * 단말기 권한설정 이동 메소드
     */
    private fun goSettingPermission() {
        Log.w(TAG, "goSettingPermission()")
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(Uri.parse("package:" + applicationContext?.packageName))
        startActivity(intent)
    }

    /**
     * 사진촬영 호출 메소드
     */
    private fun takePicture() {
        Log.w(TAG, "takePictureUrl()")
        val builder: StrictMode.VmPolicy.Builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        createImageFile()?.let {
            val outuri = FileProvider.getUriForFile(applicationContext, AUTHORITY, it)
            val intent = Intent()
            imageUri = Uri.fromFile(createImageFile())
            intent.action = MediaStore.ACTION_IMAGE_CAPTURE
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outuri)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, REQUEST_TAKE_PICTURE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.w(
            TAG,
            "Manifest.permission.READ_EXTERNAL_STORAGE(), requestCode : $requestCode, permissions : $permissions, grantResults : $grantResults"
        )
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_EXT_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage()
                } else {
                    showPopupForPermission()
                }
                return
            }
            PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture()
                } else {
                    showPopupForPermission()
                }
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_IMAGE -> { //갤러리 사진
                    Log.e(TAG, "REQUEST_PICK_IMAGE")
                    data?.clipData?.let {
                        for (i in 0 until it.itemCount) {
                            if(mIsHorizontal == true)
                                horizontalImageAdapter?.addImage(it.getItemAt(i).uri.toString())
                            else
                                verticalImageAdapter?.addImage(it.getItemAt(i).uri.toString())
                        }
                    }
                    data?.let {
                        if (it.data != null) {
                            if(mIsHorizontal == true)
                                horizontalImageAdapter?.addImage(it.data.toString())
                            else
                                verticalImageAdapter?.addImage(it.data.toString())
                        }
                    }
                }
                REQUEST_TAKE_PICTURE -> {
                    if (file != null) {
                        if(mIsHorizontal == true)
                            horizontalImageAdapter?.addImage(imageUri.toString())
                        else
                            verticalImageAdapter?.addImage(imageUri.toString())
                    }
                }
            }
        }
    }
}
