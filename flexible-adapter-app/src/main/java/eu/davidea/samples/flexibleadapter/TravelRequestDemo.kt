package eu.davidea.samples.flexibleadapter

import android.os.Bundle
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
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
        exampleAdapter.updateDataSet(listOf(
            RequestDetailsHeader(RequestDetailsModel("Here go details")),
            RequestDetailsTab(RequestDetailsTabModel("Details", "Expenses"))
        ))
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
        FlexibleViewHolder(view, adapter, false) {
        val textView by lazy(LazyThreadSafetyMode.NONE) {
            view.findViewById<TextView>(R.id.tr_details_title)
        }
    }
}

data class RequestDetailsModel(val title: String)

class RequestDetailsTab(
    private val internalModel: RequestDetailsTabModel
) : AbstractFlexibleItem<RequestDetailsTab.RequestDetailsTabViewHolder>(),
    IHolder<RequestDetailsTabModel> {

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>
    ): RequestDetailsTabViewHolder {
        return RequestDetailsTabViewHolder(view, adapter)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: RequestDetailsTabViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.tab1(model.title1)
        holder.tab2(model.title2)
    }

    override fun getModel(): RequestDetailsTabModel = internalModel

    override fun equals(other: Any?): Boolean {
        return if (other is RequestDetailsTabModel) {
            other == internalModel
        } else {
            false
        }
    }

    override fun getLayoutRes(): Int = R.layout.tr_request_tab

    class RequestDetailsTabViewHolder(val view: View, adapter: FlexibleAdapter<*>) :
        FlexibleViewHolder(view, adapter, true) {
        private val tabLayout by lazy(LazyThreadSafetyMode.NONE) {
            view.findViewById<TabLayout>(R.id.tabLayout)
        }

        fun tab1(text: String) {
            tabLayout.getTabAt(0)?.text = text
        }

        fun tab2(text: String) {
            tabLayout.getTabAt(1)?.text = text
        }
    }
}

data class RequestDetailsTabModel(
    val title1: String,
    val title2: String
)
