package com.klk.dailyjournal

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.klk.dailyjournal.data.NoteEntity
import com.klk.dailyjournal.data.NoteRepository
import com.klk.dailyjournal.entities.Feeling
import com.klk.dailyjournal.service.MoodImageStore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.notes_card.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import com.klk.dailyjournal.MainActivity as MainActivity


class MainActivity : AppCompatActivity() {

    val feeling: Feeling = Feeling.OK

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        imgFace1.setOnClickListener {
            MoodImageStore.add(1)
            setBorderForImg(1)
        }
        imgFace2.setOnClickListener {
            MoodImageStore.add(2)
            setBorderForImg(2)
        }
        imgFace3.setOnClickListener {
            MoodImageStore.add(3)
            setBorderForImg(3)
        }
        imgFace4.setOnClickListener {
            MoodImageStore.add(4)
            setBorderForImg(4)
        }
        imgFace5.setOnClickListener {
            MoodImageStore.add(5)
            setBorderForImg(5)
        }

        NoteRepository.initialize(this)
        setupDataObserver()

        doneBtn.setOnClickListener { doneBtnClicked() }
        buttonsContainer.visibility = View.INVISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun doneBtnClicked() {
        val repo = NoteRepository.get()

        val dayOfWeek = LocalDate.now().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val month = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        val date = LocalDate.now().dayOfMonth
        var dateToSave = "$month $date"
        if(date==1 || date==21 || date==31) dateToSave += "st"
        else if(date==2 || date==22) dateToSave += "nd"
        else if(date==3 || date==23) dateToSave += "rd"
        else dateToSave += "th"

        repo.insert(NoteEntity(0,
            dayOfWeek,
            dateToSave,
            MoodImageStore.getImageId().toString(),
            null,
            null,
            null,
            null,
            null,
            null))

        setBorderForImg(0)
        buttonsContainer.visibility = View.INVISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun makeJournalNote(view: View){

        //pass info to the second intent
        val intent = Intent(this, SecondActivity::class.java)
        intent.putExtra("feeling_passed", feeling.name)
        startActivity(intent)
        setBorderForImg(0)
        buttonsContainer.visibility = View.INVISIBLE
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBorderForImg(img: Int) {
        MoodImageStore.add(img)

        buttonsContainer.visibility = View.VISIBLE

        val color = ContextCompat.getColor(applicationContext,
            R.color.blue_darker)

        imgFace1.setBackgroundColor(Color.TRANSPARENT)
        imgFace2.setBackgroundColor(Color.TRANSPARENT)
        imgFace3.setBackgroundColor(Color.TRANSPARENT)
        imgFace4.setBackgroundColor(Color.TRANSPARENT)
        imgFace5.setBackgroundColor(Color.TRANSPARENT)
        when (img) {
            1 -> imgFace1.setBackgroundColor(color)
            2 -> imgFace2.setBackgroundColor(color)
            3 -> imgFace3.setBackgroundColor(color)
            4 -> imgFace4.setBackgroundColor(color)
            5 -> imgFace5.setBackgroundColor(color)
        }
    }

    private fun setupDataObserver() {
        val repo = NoteRepository.get()
        val getAllObserver = Observer<List<NoteEntity>>{ notes ->
            val adapter: ListAdapter = NotesAdapter(this, notes)
            notesList.adapter = adapter
        }
        repo.getAll().observe(this, getAllObserver)

    }

    internal class NotesAdapter(context: Context, private val notes: List<NoteEntity>)
        : ArrayAdapter<NoteEntity>(context, 0, notes)
    {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun getView(position: Int, v: View?, parent: ViewGroup): View {
            var v1: View? = v
            if (v1 == null) {
                val mInflater = LayoutInflater.from(context)
                v1 = mInflater.inflate(R.layout.notes_card, null)

            }
            val resView: View = v1!!

            val n = notes[position]
            val dateView = resView.findViewById<TextView>(R.id.tvDate)
            val textView = resView.findViewById<TextView>(R.id.tvNote)
            val moodView = resView.findViewById<ImageView>(R.id.imgMoodIcon)
            val addressView = resView.findViewById<TextView>(R.id.tvAddress)
            val imageView = resView.findViewById<ImageView>(R.id.takenPhoto1)
            val btnReadMore = resView.findViewById<Button>(R.id.btnReadMore)
            val imageID = MoodImageStore

            val date = n.dayOfWeek + ", " + n.date
            dateView.text = date
            textView.text = n.note
            moodView.setImageResource(GetImageId(n.mood.toInt()))
            addressView.text = n.address

            if(n.image?.isNotEmpty() == true){
                setImage(n, imageView)
            }

            btnReadMore.setOnClickListener{
                imageID.add(n.mood.toInt())
                var i = Intent(context, EditActivity::class.java)
                i.putExtra("id", n.id)
                i.putExtra("note", n.note)
                i.putExtra("best", n.bestPartOfDay)
                i.putExtra("grate", n.gratefulFor)
                i.putExtra("date", n.date)
                i.putExtra("address", n.address)
                i.putExtra("mood", n.mood)
                i.putExtra("dayOfWeek", n.dayOfWeek)
                i.putExtra("image", n.image)
                i.putExtra("location",n.locationLatLng)

                context.startActivity(i)
            }

            return resView
        }

        private fun setImage(n: NoteEntity, imageView: ImageView?) {
            val bmOptions = BitmapFactory.Options()
            var bitmap = BitmapFactory.decodeFile(n.image?.toString(), bmOptions)
            if(bitmap!=null) {
                val ratio = bitmap.height / bitmap.width
                val heigh = 400
                val width = heigh/ratio
                bitmap = Bitmap.createScaledBitmap(bitmap, width, heigh, false)
                imageView?.setImageBitmap(bitmap)
            }
        }


        fun GetImageId(eyes: Int): Int {
            if (eyes == 1) return R.drawable.mood_icon1
            if (eyes == 2) return R.drawable.mood_icon2
            if (eyes == 3) return R.drawable.mood_icon3
            if (eyes == 4) return R.drawable.mood_icon4
            return R.drawable.mood_icon5
        }

    }
}