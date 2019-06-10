package eu.davidea.samples.flexibleadapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.*
import eu.davidea.viewholders.FlexibleViewHolder
import kotlinx.android.synthetic.main.tr_demo.*

class TravelRequestDemo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tr_demo)

        val exampleAdapter = FlexibleAdapter(emptyList(), null).apply {
            setStickyHeaders(true)
            setDisplayHeadersAtStartUp(true)
        }
        tr_recycler_view?.apply {
            layoutManager = SmoothScrollLinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = exampleAdapter
        }

        val requestDetailsHeader = RequestDetailsHeader(RequestDetailsModel("Here go details"))
        val detailsTabLayout = RequestDetailsTab(RequestDetailsTabModel("Details", "Expenses"))
        val details = ContainerItem(
            ContainerItemModel.details(),
            detailsTabLayout
        )

        exampleAdapter.updateDataSet(
            listOf(
                requestDetailsHeader,
                details
            )
        )
    }
}

class RequestDetailsHeader(
    private val internalModel: RequestDetailsModel
) : AbstractFlexibleItem<RequestDetailsHeader.RequestDetailsViewHolder>(),
    IHolder<RequestDetailsModel> {

    override fun getModel(): RequestDetailsModel = internalModel

    override fun equals(other: Any?): Boolean {
        return if (other is RequestDetailsHeader) {
            internalModel == other.model
        } else {
            false
        }
    }

    override fun hashCode(): Int = model.hashCode()

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
        FlexibleViewHolder(view, adapter) {
        val textView by lazy(LazyThreadSafetyMode.NONE) {
            view.findViewById<TextView>(R.id.tr_details_title)
        }
    }
}

data class RequestDetailsModel(val title: String)

class RequestDetailsTab(
    private val internalModel: RequestDetailsTabModel
) : AbstractHeaderItem<RequestDetailsTab.RequestDetailsTabViewHolder>(),
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
        return if (other is RequestDetailsTab) {
            internalModel == other.model
        } else {
            false
        }
    }

    override fun hashCode(): Int = internalModel.hashCode()

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

class ContainerItem(
    private val internalModel: ContainerItemModel,
    header: RequestDetailsTab
) : AbstractSectionableItem<ContainerItem.Holder, RequestDetailsTab>(header),
    IHolder<ContainerItemModel> {
    override fun getModel(): ContainerItemModel = internalModel

    override fun equals(other: Any?): Boolean {
        return if (other is ContainerItem) {
            internalModel == other.model
        } else {
            false
        }
    }

    override fun hashCode(): Int = model.hashCode()

    override fun getLayoutRes(): Int = R.layout.tr_container_item

    override fun createViewHolder(
        view: View,
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>
    ): Holder {
        return Holder(view, adapter)
    }

    override fun bindViewHolder(
        adapter: FlexibleAdapter<IFlexible<RecyclerView.ViewHolder>>,
        holder: Holder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        holder.bind(internalModel.items)
    }

    class Holder(val view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {
        private val recyclerView by lazy(LazyThreadSafetyMode.NONE) {
            view.findViewById<RecyclerView>(R.id.container)
        }

        fun bind(items: List<String>) {
            val dummyAdapter = DummyAdapter(items)
            recyclerView.apply {
                layoutManager = LinearLayoutManager(context)
                itemAnimator = null
                adapter = dummyAdapter
            }
        }
    }
}

data class ContainerItemModel(
    val items: List<String>
) {
    companion object {
        fun expenses(): ContainerItemModel {
            return ContainerItemModel(
                (1..100).map { "Expense $it" }.toList()
            )
        }

        fun details(): ContainerItemModel {
            return ContainerItemModel(
                (1..100).map { "Form Field $it" }.toList()
            )
        }
    }
}

class DummyAdapter(var items: List<String> = emptyList()) :
    RecyclerView.Adapter<DummyAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class Holder(val containerView: View) : RecyclerView.ViewHolder(containerView) {
        private val textView by lazy(LazyThreadSafetyMode.NONE) {
            containerView.findViewById<TextView>(android.R.id.text1)
        }

        fun bind(text: String) {
            textView.text = text
        }
    }
}
