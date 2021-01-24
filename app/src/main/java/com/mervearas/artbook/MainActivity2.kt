package com.mervearas.artbook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main2.*
import java.io.ByteArrayOutputStream
import java.lang.Exception

class MainActivity2 : AppCompatActivity() {

    var selectedPicture : Uri? = null  // ********* 5.kısımdan gelinip yapıldı *********
    var selectedBitmap : Bitmap? = null  // ********** önce 5. kısımda sonra 6. kısımda kullanıldı **********

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // ----------------------------------------------------------- NEW / OLD SORGUSU (9.bölümle bağlı) ------------------------------------------------------------------
        // ********************************* 10 ******************************************
        val intent = intent
        val info = intent.getStringExtra("info") //mainactivity 9.bölümde name:info olan Intent'i getirir.
        if (info.equals("new")){ //eğer menu kısmına basılıp yeni resim eklenecekse bu kısım çalışır.
            artText.setText("")
            artistText.setText("")
            yearText.setText("")
            button.visibility = View.VISIBLE

            val selectedImageBackground =  BitmapFactory.decodeResource(applicationContext.resources,R.drawable.selectimage) //resources klasörü altındaki drawble içindeki selectedimage isimli resme erişmeyi sağlar. Bu resmi selectedImageBackground değişkeninin içine atar.
            imageView.setImageBitmap(selectedImageBackground) // imageView kısmına selectedImageBackground'a atanan resmi koyar

            } else{ //eğer mainactiviy kısmındaki manu yerine listview içeriğinde kaydedilmiş verilere basıldıysa (yani old ise) aşağıdakiler yapılır.
            button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1) //mainactivity kısmında tanımlanan Intent içindeki id'yi aldı. defaulValue 1 yazıldı bunun anlamı bir sorun çıkarsa 1 numaralı id'yi göster.

            // ----------------------------------------------------------- SEÇİM ARGÜMANLARI ------------------------------------------------------------------
            // ********************************* 11 ******************************************
            try {
                val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString())) //Seçilen id bilinmediği için string türünde liste oluşturulup selectedId içine yazıldı.
                val artnameIndex = cursor.getColumnIndex("artname")
                val artistnameIndex = cursor.getColumnIndex("artistname")
                val yearIndex = cursor.getColumnIndex("year")
                val imageIndex = cursor.getColumnIndex("image")

                while (cursor.moveToNext()){
                    artText.setText(cursor.getString(artnameIndex))
                    artistText.setText(cursor.getString(artistnameIndex))
                    yearText.setText(cursor.getString(yearIndex))
                    //----------------------GÖRSELİ ALMA İŞLEMİ ------------------------------
                    val byteArray = cursor.getBlob(imageIndex)  //image index'i byteArray olarak aldık
                    val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size) //byteArray şeklinde olan veriyi bitmap'e çevirdik. byteArray'in uzunluğu ne kadarsa o kadar veriyi bitmap'e çevirdi.
                    imageView.setImageBitmap(bitmap) //imageView'ın içine koydu.
                }
                cursor.close()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    fun save (view: View){
        //-------------------------------------------------------------- KULLANICIDAN VERİLERİ ALMA --------------------------------------------------------------

        // ********************************* 6 ******************************************
        val artName = artText.text.toString()
        val artistName = artistText.text.toString()
        val year = yearText.text.toString()

        if (selectedBitmap != null){  //******** 7.kısımdan gelindi*********
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300) //7.kısımda oluştutulan fonksiyon kullanıldı.
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream) //compress : seçtiğimiz resmi sıkıştırıp veriye çeviren bir metod. Resim PNG formatına dönüştürüldü. quality resimin kalitesidir 0-100 arası değer verilir. outputstream, compress işleminin yapılması için gereklidir ve ByteArrayOutPutStream şeklindedir.
            val byteArray = outputStream.toByteArray() //******* 7.kısım sonu *******

            try {
                //-------------------------------------------------------------- VERİ TABANI OLUŞTURUP VERİ KAYDETME --------------------------------------------------------------
                // ********************************* 8 ******************************************
                val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts(id INTEGER PRIMARY KEY, artname VARCHAR, artistname VARCHAR, year VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO arts (artname, artistname,year,image) VALUES(?,?,?,?)"
                val statement = database.compileStatement(sqlString) //Yukarıda tanımlanan string şeklindeki kodu SQL olarak yazar
                statement.bindString(1, artName) //soru işareti olan kısımları değerleriyle eşleştirir. sırasıyla yazılması gerekir.
                statement.bindString(2, artistName) //soru işareti olan kısımları değerleriyle eşleştirir. sırasıyla yazılması gerekir.
                statement.bindString(3, year) //soru işareti olan kısımları değerleriyle eşleştirir. sırasıyla yazılması gerekir.
                statement.bindBlob(4, byteArray) //soru işareti olan kısımları değerleriyle eşleştirir. sırasıyla yazılması gerekir.
                statement.execute() //bütün sql işlemini bitirip sql'e kaydetme kodu
            }catch (e:Exception){
                e.printStackTrace()
            }

            // ********************************* 12 ******************************************
            val intent = Intent(this,MainActivity::class.java) //save yaptıktan sonra ana aktivite sayfasına gidip orada kayıtlı olan verileri günceller
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) //Kendisinden önceki bütün aktivite sayfalarını kapatır ve eğer geri çıkmak istersek direkt uygulamadan çıkar.
            startActivity(intent)
            //finish()
        }
    }

    //------------------------------------------------------ BİTMAP BOYUTU KÜÇÜLTME --------------------------------------------------------------
    // ********************************* 7 ******************************************
    fun makeSmallerBitmap (image:Bitmap, maximumSize:Int):Bitmap {  //image ve maximumsize değerlerini alıp geriye bitmap döndüren fonksiyon
        var width = image.width  //resmin genişlik bilgisi alındı
        var height = image.height  //resmin yükseklik bilgisi alındı.

        val bitmapRatio : Double = width.toDouble() / height.toDouble()  //resmin boyu mu yoksa eni mi büyük bu tespit edilir.
        if (bitmapRatio > 1){  //bitmapRatio 1'den büyükse resim yataydır
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        } else{ //bitmapRatio 1'den küçükse resim dikeydir.
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    // ----------------------------------------------------- İZİN İSTEME BÖLÜMÜ --------------------------------------------------------------------
    fun selectImage(view:View){
        // ********************************* 3 **************************************
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {  //contexcompat izinlerin sdk'ler arası uyumlu çalışmasını sağlar. checkselfpermission ise izinler bu kod ile alınıp alınmadığı kontrol edilir.

            ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),1) //if komutunda kulanıcının izin verip vermediği sorgulandı ve izin vermediyse bu kod satırında read.external.storage içindeki izinleri kullanıcıdan istedik. ActivityCompat yazılma sebebi ContextCompat ile aynıdır.
            //requestPermissions içerisinde contex belirttikten sonra liste içerisinde hangi izinleri istediğimiz yazılır. son olarak requestCode yazılır. Buna kendimiz sayı veririz

        }else{  //izin varsa galeriye gitmek için kodlar burada yazıldı

            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //Intent ile galeriye gidilir action_pick alma işlemini yapar mediastore kısmı ise verinin galeride nerede tutulduğunu bilmemizi sağlar.
            startActivityForResult(intentToGallery,2) //yukarıdaki intent'i başlatmak için bu kod yazıldı.
        }
    }

    // ********************************* 4 ******************************************
    override fun onRequestPermissionsResult(  //izin isteğinin sonucunda ne yapılacağı yazar yani galeriye gitme izni alındıysa izin verildiği gibi otomatik olarak galerinin açılmasını bu kod altında yazarız.
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray  //grantResults : izin verilip verilmediğini içinde barındıran integer bir dizidir.
    ) {
        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){  //grantResults'ın boyu 0'dan büyük mü değil mi yani eğer izin verildiyse size > 0 olacağı için izin verilip verilmediği buradan anlaşılabilir. Ve grantResults'un 0.index'i packageManager içindeki Permission_Granded'a eşitse yani izin verilmişse anlamına gelir. eğer Permission_Denied olsaydı izin verilmemiş anlamına gelirdi.
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)  //Yukarıdaki else kısmının aynısı kopyala-yapıştır yapıldı. izin verilmişse galeriye gidildi.
                startActivityForResult(intentToGallery,2)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    // ********************************* 5 ******************************************
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //burada yazan data bize URI verir biz onu bitmap'e çevirmeliyiz.
        if(requestCode == 2 && resultCode == RESULT_OK && data != null){ //requestcode = 2 (yani izin verilmişsse) ve resultcode = result_ok (yani kullanıcı bir resim seçtiyse) ve data != null (resim seçip seçmediğinden iyice emin olmak için yazıldı. eğer null olursa resim seçmemiştir)
            selectedPicture = data.data //data içindeki URI bilgi selectedPicture içine aktarıldı.

            if (selectedPicture != null){

                if (Build.VERSION.SDK_INT >=28){  //SDK versiyon 28'den büyükse bu kısım çalışacak
                    val source = ImageDecoder.createSource(this.contentResolver,selectedPicture!!) //ImageDecoder : resimleri bitmap'e dönüştürmeye yarayan sınıf.
                    selectedBitmap = ImageDecoder.decodeBitmap(source) //yeni nesil bitmap oluşturma versiyonu SDK 28 ve sonrası için
                    imageView.setImageBitmap(selectedBitmap)

                }else{  //SDK versiyon 28'den küçükse bu kısım çalışacak
                    selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,selectedPicture) //seçilen resmin Uri'ını bitmap şeklinde çözümler (contentResolver : içerik çözümleyizi) . getBitmap artık kullanılmadığı için üstü çizili fakat bunun yerine yeni kullanılan kod ise eski telefonlarda kullanılmayabilir. bu yüzden hem getBitmap hemde yeni çıkan kod kullanılıp telefon modeline göre işlem yaptırılacak.
                    imageView.setImageBitmap(selectedBitmap)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}