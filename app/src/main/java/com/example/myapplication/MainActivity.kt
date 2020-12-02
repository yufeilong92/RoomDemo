package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.example.myapplication.ui.UserViewModel
import com.example.myapplication.ui.ViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var viewModelFactory: ViewModelFactory
    private val viewModel: UserViewModel by viewModels { viewModelFactory }

    private val disposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModelFactory = Injection.provideViewModelFactory(this)
        btn_add.setOnClickListener {
            upDataName()
        }
    }

    override fun onStart() {
        super.onStart()
        disposable.add(
            viewModel.userName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tv_content.text = it
                }, {})
        )

    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    private fun upDataName() {
        val com = et_content.text.toString()
        disposable.add(
            viewModel.updateUserName(com)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({

                }, {

                })

        )

    }
}
