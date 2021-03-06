package com.skydoves.waterdays.ui.activities.main

import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.github.skydoves.ElasticAction
import com.jakewharton.rxbinding2.view.RxView
import com.skydoves.waterdays.R
import com.skydoves.waterdays.compose.BaseActivity
import com.skydoves.waterdays.compose.qualifiers.RequirePresenter
import com.skydoves.waterdays.consts.CapacityDrawable
import com.skydoves.waterdays.models.Capacity
import com.skydoves.waterdays.presenters.SelectDrinkPresenter
import com.skydoves.waterdays.ui.adapters.SelectDrinkAdapter
import com.skydoves.waterdays.ui.viewholders.SelectDrinkViewHolder
import com.skydoves.waterdays.viewTypes.SelectDrinkActivityView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_select_drink.*

/**
 * Created by skydoves on 2016-10-15.
 * Updated by skydoves on 2017-09-22.
 * Copyright (c) 2017 skydoves rights reserved.
 */

@RequirePresenter(SelectDrinkPresenter::class)
class SelectDrinkActivity : BaseActivity<SelectDrinkPresenter, SelectDrinkActivityView>(), SelectDrinkActivityView {

    private lateinit var adapter: SelectDrinkAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_drink)
        initBaseView(this)

        RxView.clicks(findViewById(R.id.selectdrink_btn_add))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { e -> addCapacity() }

        val view = findViewById(R.id.icon_question)
        RxView.clicks(view)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { Snackbar.make(view, getString(R.string.msg_press_long), Snackbar.LENGTH_LONG).setActionTextColor(Color.WHITE).show() }

        RxView.clicks(findViewById(R.id.selectdrink_btn_close))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { finish() }
    }

    override fun initBaseView(selectDrinkActivityView: SelectDrinkActivityView) {
        super.initBaseView(selectDrinkActivityView)
    }

    override fun initializeUI() {
        adapter = SelectDrinkAdapter(delegate)
        selectdrink_rcyv.setAdapter(adapter)

        val capacities = presenter.capacityItemList
        if (capacities != null) {
            for (capacity in capacities) {
                val amount = capacity.amount
                val drawable = ContextCompat.getDrawable(baseContext, CapacityDrawable.getLayout(amount))
                adapter!!.addCapacityItem(Capacity(drawable, amount))
            }

            if (capacities.size == 0)
                Toast.makeText(baseContext, R.string.msg_require_capacity, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * recyclerView onTouch listeners delegate
     */
    internal var delegate: SelectDrinkViewHolder.Delegate = object : SelectDrinkViewHolder.Delegate {
        override fun onClick(view: View, capacity: Capacity) {
            val duration = 200
            ElasticAction.doAction(view, duration, 0.9f, 0.9f)

            Handler().postDelayed({
                presenter.addRecrodItem(capacity.amount)
                finish()
            }, duration.toLong())
        }

        override fun onLongClick(view: View, capacity: Capacity) {
            val alertDlg = AlertDialog.Builder(view.context)
            alertDlg.setTitle(getString(R.string.title_alert))

            // yes - delete
            alertDlg.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface, which: Int ->
                presenter.deleteCapacity(capacity)
                adapter.removeDrinkItem(capacity)
                Toast.makeText(baseContext, capacity.amount.toString() + "ml " + getString(R.string.msg_delete_capacity), Toast.LENGTH_SHORT).show()
            }

            // no - cancel
            alertDlg.setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, which: Int -> dialog.dismiss() }
            alertDlg.setMessage(String.format(getString(R.string.msg_ask_remove_capacity)))
            alertDlg.show()
        }
    }

    /**
     * add a new water capacity cup
     */
    private fun addCapacity() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle(getString(R.string.title_add_capacity))
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setRawInputType(Configuration.KEYBOARD_12KEY)
        alert.setView(input)
        alert.setPositiveButton(getString(R.string.yes)) { dialog: DialogInterface, whichButton: Int ->
            try {
                val amount = Integer.parseInt(input.text.toString())
                if (amount > 0 && amount < 3000) {
                    val capacity = Capacity(ContextCompat.getDrawable(baseContext, CapacityDrawable.getLayout(amount)), amount)
                    presenter.addCapacity(capacity)
                    adapter.addCapacityItem(capacity)
                } else
                    Toast.makeText(baseContext, R.string.msg_invalid_input, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        alert.setNegativeButton(getString(R.string.no)) { dialog: DialogInterface, whichButton: Int ->

        }
        alert.show()
        val mgr = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mgr.showSoftInputFromInputMethod(input.applicationWindowToken, InputMethodManager.SHOW_FORCED)
    }
}