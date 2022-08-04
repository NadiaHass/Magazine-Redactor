package com.nadiahassouni.magazinewriter.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nadiahassouni.magazinewriter.databinding.FragmentUpdateArticleBinding
import com.nadiahassouni.magazinewriter.model.Article
import com.nadiahassouni.magazinewriter.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class UpdateArticleFragment : Fragment() {
    private var _binding: FragmentUpdateArticleBinding? = null
    private val binding get() = _binding!!
    private var imagesUri: Uri = Uri.parse("")
    private lateinit var storageReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View{
        _binding = FragmentUpdateArticleBinding.inflate(inflater, container, false)

        storageReference = FirebaseStorage.getInstance().getReference("articlePictures")

        val article = arguments?.getSerializable("article") as Article

        setTypesSpinner()
        putData(article)
        getCategories()

        binding.btnUpdateArticle.setOnClickListener {
            binding.btnUpdateArticle.visibility = View.GONE
            showProgressBar()
            uploadPictureToStorage(imagesUri , article.id , article.imageUrl , binding.spinnerType.selectedItem.toString() ,
                binding.spinner.selectedItem.toString() )
        }

        binding.ivArticle.setOnClickListener {
            openFileChooser()
        }

        return binding.root
    }

    private fun putData(article: Article) {
        binding.etTitle.setText(article.title)
        binding.etContent.setText(article.text)
        Glide.with(requireContext())
            .load(article.imageUrl)
            .into(binding.ivArticle)
    }

    private fun setTypesSpinner() {
        val list = ArrayList<String>()
        list.add("magazine")
        list.add("article")
        val arrayAdapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_dropdown_item , list)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = arrayAdapter

    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data?.data != null) {
                    imagesUri = data.data!!
                    Glide.with(requireContext())
                        .load(imagesUri)
                        .into(binding.ivArticle)
                }
            }
        }

    private fun uploadPictureToStorage(
        uriImage: Uri?,
        id: String,
        imageUrl: String,
        type: String,
        category: String
    ) = CoroutineScope(
        Dispatchers.IO).launch {
        if (uriImage != Uri.parse("")) {
            val fileReference =
                storageReference.child(UUID.randomUUID().toString() + getFileExtension(uriImage!!))
            var image = String()
            fileReference.putFile(uriImage).addOnSuccessListener { l ->
                fileReference.downloadUrl.addOnSuccessListener { uri ->
                    image = uri.toString()

                    val sdf = SimpleDateFormat("yyyy-MM-dd").format(Date())

                    val article = Article(id, binding.etTitle.text.toString() , image,
                        binding.etContent.text.toString(), category,  sdf ,
                        "en attente" , type)

                    updateArticle(article)
                }
            }
        }else{
            val sdf = SimpleDateFormat("yyyy-MM-dd").format(Date())
            val article = Article(id, binding.etTitle.text.toString() , imageUrl,
                binding.etContent.text.toString(),binding.spinner.selectedItem.toString() ,  sdf ,
            "" , binding.spinnerType.selectedItem.toString())

            updateArticle(article)

        }
    }

    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }

    private fun getFileExtension(uriImage: Uri): String? {
        val contentResolver = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver?.getType(uriImage))
    }

    private fun updateArticle(article: Article) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val storyCollectionRef = Firebase.firestore.collection("articles")
            val snapshot = storyCollectionRef.whereEqualTo("id", article.id)
                .get().await()
            for (doc in snapshot.documents) {
                doc.reference.update(mapOf("title" to article.title,
                    "imageUrl" to article.imageUrl, "date" to article.date , "text" to article.text ,
                "category" to article.category , "type" to article.type , "state" to article.state))
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(requireContext() , "L'article a ete modifiee'" , Toast.LENGTH_SHORT).show()
                binding.btnUpdateArticle.visibility = View.VISIBLE
                hideProgressBar()
                startActivity(Intent(context , MainActivity::class.java))
                activity?.finish()
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
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