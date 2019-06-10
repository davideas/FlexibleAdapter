package eu.davidea.samples.flexibleadapter

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.flexibleadapter.items.IHolder
import eu.davidea.viewholders.FlexibleViewHolder
import kotlinx.android.synthetic.main.tr_demo.*

class TravelRequestDemo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tr_demo)

        val exampleAdapter = FlexibleAdapter(emptyList(), null)
        tr_recycler_view?.apply {
            layoutManager = SmoothScrollLinearLayoutManager(context)
            adapter = exampleAdapter
        }
        exampleAdapter.updateDataSet(listOf(RequestDetailsHeader(RequestDetailsModel("Hi"))))
    }
}

class RequestDetailsHeader(
    private val internalModel: RequestDetailsModel
) : AbstractFlexibleItem<RequestDetailsHeader.RequestDetailsViewHolder>(),
    IHolder<RequestDetailsModel> {

    override fun getModel(): RequestDetailsModel = internalModel

    override fun equals(other: Any?): Boolean {
        return if (other is RequestDetailsModel) {
            other == internalModel
        } else {
            false
        }
    }

    override fun getLayoutRes(): Int = R.layout.tr_request_details

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>
    ): RequestDetailsViewHolder {
        return RequestDetailsViewHolder(view, adapter)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: RequestDetailsViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.textView.text = model.title
    }

    class RequestDetailsViewHolder(val view: View, adapter: FlexibleAdapter<*>) :
        FlexibleViewHolder(view, adapter, true) {
        val textView by lazy(LazyThreadSafetyMode.NONE) {
            view.findViewById<TextView>(R.id.tr_details_title)
        }
    }
}

data class RequestDetailsModel(val title: String)
