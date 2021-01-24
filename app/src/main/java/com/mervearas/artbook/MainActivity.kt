package com.mervearas.artbook

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //------------------------------------------------------ VERİLERİ ALMAK --------------------------------------------------------------
        // ********************************* 9 ******************************************
        val artNameList = ArrayList<String>()
        val artIdList = ArrayList<Int>()
        val arrayAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,artNameList)
        listView.adapter = arrayAdapter

        try {
            val database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM arts",null)
            val artnameIndex = cursor.getColumnIndex("artname")
            val idIndex = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                artNameList.add(cursor.getString(artnameIndex))
                artIdList.add(cursor.getInt(idIndex))
            }
            arrayAdapter.notifyDataSetChanged() //verileri listview içine koyar
            cursor.close()
        }catch (e:Exception){
            e.printStackTrace()
        }

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> //onItemClickListener : ListView'a tıklanınca ne olacağını yazmamızı sağlar.
            val intent = Intent(this,MainActivity2::class.java)
            intent.putExtra("info","old") //mainactivity'den basılınca main2activity görünümünde önceki kaydedilen bilgilerin açılması için old değerinde bir intent oluşturuldu.
            intent.putExtra("id",artIdList[position])
            startActivity(intent)
        }
    }

    //--------------------------------------------------------------MENU İLE İLGİLİ İŞLEMLER--------------------------------------------------------------
    // ********************************* 1 ******************************************
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {  //oluşturulan menuyü mainactivity'e bağlar.
        //Inflater : res dosyası oluşturduğumuzda o res dosyasındaki şeyi mainactivity'e bağlamak için çeşitli Inflater'ler kullanılır. Aşağıda menuInflater kullanıldı çünkü res doyasında menu oluşturuldu.
        val menuInflater = menuInflater  //add.art.xml dosyasında oluşturulan menüyü mainactivity'e bağlamak için menuInflater kullanılır.
        menuInflater.inflate(R.menu.add_art,menu)  //hangi res dosyasını kullanacağımızı belirtiyoruz. Resorses içindeki menü klasöründeki add_art'ı kullanacağımızı belirtiyoruz. menu yazan yer ise fonksiyonda yazan menu.
        return super.onCreateOptionsMenu(menu)
    }

    // ********************************* 2 ******************************************
    override fun onOptionsItemSelected(item: MenuItem): Boolean {  //seçilen ıtem'ın ne olduğunu anlayıp ona göre işlem yaptırır.
        if (item.itemId == R.id.add_art_item){ //oluşturulan menü içinde hangi veriye tıklandığı verilerin id'leri yardımıyla sorgulanır.
            val intent = Intent(this,MainActivity2::class.java) //eğer add_art_item id'li veriye tıklanırsa 2.aktiviteye git kodu çalışacak.
            intent.putExtra("info","new") //mainactivity'den menu kısmına basılınca main2activity'deki kaydetme görünümüne gitmesi için yazıldı ***9.bölüm***
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}