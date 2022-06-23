package com.example.chatapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.daos.PostDao
import com.example.chatapp.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_in.*

class MainActivity : AppCompatActivity(), IPostAdapter {

    private lateinit var postDao: PostDao
    private lateinit var adapter: PostAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = Firebase.auth
        postDao = PostDao()

        fab.setOnClickListener {_ ->
            val alert = AlertDialog.Builder(this)
            alert.setTitle("Add Post ")
            alert.setMessage("Enter Post To Add")
            val editTask = EditText(this)
            alert.setView(editTask)

            alert.setPositiveButton("Post"){dialog, _ ->
                val postText= editTask.text.toString().trim()
                if(postText.isNotEmpty()){
                    postDao.addPost(postText)
                    Toast.makeText(this,"${postText} Posted Syccesfully", Toast.LENGTH_SHORT).show()
                }
                editTask.text.clear()
                dialog.dismiss()
            }
            alert.setNegativeButton("Cancel"){dialog, _ ->
                Toast.makeText(this,"Clicked Cancel", Toast.LENGTH_SHORT).show()
                editTask.text.clear()
                dialog.dismiss()
            }
            alert.show()

        }
        setUpRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): kotlin.Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): kotlin.Boolean {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Do you really want to sign out?")
        alert.setPositiveButton("Yes"){dialog, _ ->
            auth.signOut()
            val i = Intent(this, SignInActivity::class.java)
            startActivity(i)
            finish()
            dialog.dismiss()
        }
        alert.setNegativeButton("Cancel"){dialog, _ ->
            Toast.makeText(this,"Clicked Cancel", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        alert.show()
        return super.onOptionsItemSelected(item)
    }


    private fun setUpRecyclerView() {
        postDao = PostDao()
        val postsCollections = postDao.postCollection
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)
        val recyclerViewOptions =  FirestoreRecyclerOptions.Builder<Post>().setQuery(query,Post::class.java).build()

        adapter = PostAdapter(recyclerViewOptions, this)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId)
    }

}