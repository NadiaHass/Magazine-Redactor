package com.nadiahassouni.magazinewriter.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nadiahassouni.magazinewriter.R
import com.nadiahassouni.magazinewriter.model.Article
import com.nadiahassouni.magazinewriter.databinding.FragmentAddArticleBinding
import com.nadiahassouni.magazinewriter.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddArticleFragment : Fragment() {
    private var _binding : FragmentAddArticleBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageReference : StorageReference
    private var articleId : String = ""
    private lateinit var imagesUri : Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View{
        _binding = FragmentAddArticleBinding.inflate(inflater , container , false)

        storageReference = FirebaseStorage.getInstance().getReference("articlePictures")

        binding.ivArticle.setOnClickListener {
            openFileChooser()
        }

        binding.btnAddArticle.setOnClickListener {
            showProgressBar()
            binding.btnAddArticle.visibility = View.GONE
            uploadPictureToStorage(imagesUri)
        }

        setTypesSpinner()
        getCategories()

        return binding.root
    }

    private fun setTypesSpinner() {
        val list = ArrayList<String>()
        list.add("magazine")
        list.add("article")
        val arrayAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_dropdown_item , list)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = arrayAdapter

    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if (result.resultCode == Activity.RESULT_OK ){
            val data : Intent? = result.data
            if (data?.data != null) {
                imagesUri = data.data!!
                Glide.with(requireContext())
                    .load(imagesUri)
                    .into(binding.ivArticle)
            }
        }
    }

    private fun uploadPictureToStorage(uriImage: Uri?) = CoroutineScope(Dispatchers.IO).launch {
        if(uriImage != null){
            val fileReference = storageReference.child(UUID.randomUUID().toString() + getFileExtension(uriImage))
            var image = String()
            fileReference.putFile(uriImage).addOnSuccessListener { l ->
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    image = uri.toString()

                    val sdf = SimpleDateFormat("yyyy-MM-dd").format(Date())

                    val article = Article(articleId , binding.etTitle.text.toString() , image ,
                        binding.etContent.text.toString() , binding.spinner.selectedItem.toString() , sdf ,
                    "" , binding.spinnerType.selectedItem.toString())

                    addArticleToDatabase(article)

                }
            }
        }
    }

    private fun getFileExtension(uriImage: Uri): String? {
        val contentResolver = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver?.getType(uriImage))
    }

    private fun addArticleToDatabase(article : Article) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val announceCollectionRef = Firebase.firestore.collection("articles")
            articleId = announceCollectionRef.document().id
            article.id = articleId
            announceCollectionRef.add(article).await()
            withContext(Dispatchers.Main){
                Toast.makeText(context ,"L'article a ete ajoute" , Toast.LENGTH_LONG).show()
                hideProgressBar()
                binding.btnAddArticle.visibility = View.VISIBLE
                emptyInput()
            }
        }catch (e : Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(context , e.message , Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun emptyInput() {
        binding.etTitle.text.clear()
        binding.etContent.text.clear()
        Glide.with(requireContext())
            .load(R.drawable.ic_add_photo)
            .into(binding.ivArticle)
    }

    private fun getCategories() = CoroutineScope(Dispatchers.IO).launch {
        var category : Category?

        try {
            val categoryCollectionRef = Firebase.firestore.collection("categories")
            val querySnapshot = categoryCollectionRef.get().await()
            var list = ArrayList<String>()
            for(doc in querySnapshot.documents){
                category = doc.toObject<Category>()
                if (category != null) {
                    list.add(category.title)
                }
            }
            withContext(Dispatchers.Main){
                val arrayAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_dropdown_item , list)
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinner.adapter = arrayAdapter
            }

        }catch (e : java.lang.Exception){

        }

    }
}