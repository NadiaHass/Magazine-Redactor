package com.nadiahassouni.magazinewriter.ui.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.nadiahassouni.magazinewriter.R
import com.nadiahassouni.magazinewriter.databinding.FragmentAddStoryBinding
import com.nadiahassouni.magazinewriter.databinding.FragmentArticleBinding
import com.nadiahassouni.magazinewriter.model.Article
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ArticleFragment : Fragment() {
    private var _binding : FragmentArticleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentArticleBinding.inflate(inflater , container , false)

        val article = arguments?.getSerializable("article") as Article

        putData(article)

        binding.btnDeleteArticle.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext())
                .setTitle("Suppression de l'article")
                .setMessage("Est ce que vous voulez vraiment supprimer l'article ?")
                .setPositiveButton("Oui") { p0, p1 ->
                    deleteArticle(article.id)
                }
                .setNegativeButton("Non"){ p0, p1 ->
                    p0.dismiss()
                }
            alertDialog.show()
        }

        binding.btnUpdateArticle.setOnClickListener {
            navigateToUpdateArticleFragment(article)
        }

        return binding.root
    }

    private fun navigateToUpdateArticleFragment(article: Article) {
        val bundle = Bundle()
        bundle.putSerializable("article" , article)
        Navigation.findNavController(binding.root).navigate(R.id.action_articleFragment_to_updateArticleFragment , bundle)

    }

    private fun deleteArticle(id: String)= CoroutineScope(Dispatchers.IO).launch {
        try {
            val storyCollectionRef =
                Firebase.firestore.collection("articles").whereEqualTo("id", id )
            val snapshot =  storyCollectionRef.get().await()
            for (doc in snapshot.documents){
                doc.reference.delete()
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "L'article a ete supprimee", Toast.LENGTH_LONG).show()
                startActivity(Intent(context , MainActivity::class.java))
                activity?.finish()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun putData(article: Article) {
        binding.tvTitle.text = article.title
        binding.tvCategory.text = article.category
        binding.tvContent.text = article.text
        binding.tvDate.text = article.date
        Glide.with(requireContext())
            .load(article.imageUrl)
            .into(binding.ivArticle)
    }

}