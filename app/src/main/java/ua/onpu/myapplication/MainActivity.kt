package ua.onpu.myapplication

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var sharedPreferences: SharedPreferences
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        val takeSelfieButton: Button = findViewById(R.id.takeSelfieButton)
        val sendSelfieButton: Button = findViewById(R.id.sendSelfieButton)

        sharedPreferences = getSharedPreferences("my_preferences", MODE_PRIVATE)

        // Завантаження збереженого URI зображення
        val savedImagePath = sharedPreferences.getString("image_path", null)
        savedImagePath?.let {
            imageUri = Uri.parse(it)
            imageView.setImageURI(imageUri)
        }

        // Зйомка фото
        takeSelfieButton.setOnClickListener {
            takeSelfie()
        }

        // Відправка фото
        sendSelfieButton.setOnClickListener {
            sendSelfie()
        }
    }

    // Реєстрація ActivityResultLauncher для отримання фото
    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                imageView.setImageBitmap(it)

                imageUri = saveImageToInternalStorage(it)

                with(sharedPreferences.edit()) {
                    putString("image_path", imageUri.toString())
                    apply()
                }
            }
        }
    }

    private fun takeSelfie() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.resolveActivity(packageManager)?.also {
            takePictureLauncher.launch(takePictureIntent)
        }
    }

    private fun sendSelfie() {
        if (imageUri != null) {
            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("vladis217lak10372@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "DigiJED [Kalkatin Vlad]")
                putExtra(Intent.EXTRA_TEXT, "Посилання на репозиторій із проєктом мобільного додатку: [https://github.com/eIIka/Android-apps.git]")
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(emailIntent, "Надіслати селфі через..."))
        }
    }

    // Функція для збереження зображення у внутрішньому сховищі та отримання URI через FileProvider
    private fun saveImageToInternalStorage(bitmap: Bitmap): Uri? {
        return try {
            val filename = "selfie_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, filename)
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()

            FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
