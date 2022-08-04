package com.nadiahassouni.magazinewriter.ui.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.nadiahassouni.magazinewriter.R
import com.nadiahassouni.magazinewriter.model.Story
import com.nadiahassouni.magazinewriter.databinding.FragmentAddStoryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class AddStoryFragment : androidx.fragment.app.Fragment() {
    private var _binding : FragmentAddStoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var storageReference : StorageReference
    private var storyId : String = ""
    private lateinit var imagesUri : Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddStoryBinding.inflate(inflater , container , false)

        storageReference = FirebaseStorage.getInstance().getReference("storyPictures")

        binding.ivStory.setOnClickListener {
            openFileChooser()
        }

        binding.btnAddStory.setOnClickListener {
            showProgressBar()
            binding.btnAddStory.visibility = View.GONE
            uploadPictureToStorage(imagesUri)
        }

        return binding.root
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
                    .into(binding.ivStory)
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

                    val story = Story(storyId , image , binding.etTitle.text.toString(), sdf , "")

                    addArticleToDatabase(story)

                }
            }
        }
    }

    private fun getFileExtension(uriImage: Uri): String? {
        val contentResolver = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(contentResolver?.getType(uriImage))
    }

    private fun addArticleToDatabase(story : Story) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val storyCollectionRef = Firebase.firestore.collection("stories")
            storyId = storyCollectionRef.document().id
            story.id = storyId
            storyCollectionRef.add(story).await()
            withContext(Dispatchers.Main){
                Toast.makeText(context ,"La story a ete ajoute" , Toast.LENGTH_LONG).show()
                hideProgressBar()
                binding.btnAddStory.visibility = View.VISIBLE
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
        Glide.with(requireContext())
            .load(R.drawable.ic_add_photo)
            .into(binding.ivStory)

    }

    private fun navigateToHomeFragment() {

    }
    private fun showProgressBar() {
        binding.progressBar.visibility = View.VISIBLE
    }
    private fun hideProgressBar() {
        binding.progressBar.visibility = View.GONE
    }
}