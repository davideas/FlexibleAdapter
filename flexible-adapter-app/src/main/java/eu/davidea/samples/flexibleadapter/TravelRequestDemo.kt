package eu.davidea.samples.flexibleadapter

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager
import eu.davidea.flexibleadapter.items.*
import eu.davidea.viewholders.FlexibleViewHolder
import kotlinx.android.synthetic.main.tr_demo.*

class TravelRequestDemo : AppCompatActivity() {
    private val handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tr_demo)
        setSupportActionBar(toolbar)

        swipeRefreshLayout?.setOnRefreshListener {
            handler.postDelayed({
                swipeRefreshLayout.isRefreshing = false
            }, 200)
        }

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

        val detailsTabLayout = RequestDetailsTab(
            RequestDetailsTabModel("Details", "Expenses", 0)
        )
        val details = ContainerItem(
            ContainerItemModel.details(),
            detailsTabLayout
        )

        val expenseTabLayout = RequestDetailsTab(
            RequestDetailsTabModel("Details", "Expenses", 1)
        )
        val expenses = ContainerItem(
            ContainerItemModel.expenses(),
            expenseTabLayout
        )

        val tabSelected: (Int) -> Unit = { index ->
            when {
                index == 0 -> {
                    tr_recycler_view.stopScroll()
                    exampleAdapter.updateDataSet(
                        listOf(
                            requestDetailsHeader,
                            details
                        )
                    )
                }
                index == 1 -> {
                    tr_recycler_view.stopScroll()
                    exampleAdapter.updateDataSet(
                        listOf(
                            requestDetailsHeader,
                            expenses
                        )
                    )
                }
            }
        }
        detailsTabLayout.tabListener = tabSelected
        expenseTabLayout.tabListener = tabSelected

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

    var tabListener: ((Int) -> Unit)? = null

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
        holder.bind(model, tabListener)
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
        private var tabListener: TabListener? = null
        private val tabLayout by lazy(LazyThreadSafetyMode.NONE) {
            view.findViewById<TabLayout>(R.id.tabLayout)
        }

        fun bind(model: RequestDetailsTabModel, onTabSelected: ((Int) -> Unit)?) {
            tabListener?.let { tabLayout.removeOnTabSelectedListener(it) }

            tabLayout.getTabAt(0)?.text = model.title1
            tabLayout.getTabAt(1)?.text = model.title2
            tabLayout.getTabAt(model.selected)?.select()

            if (onTabSelected == null) {
                tabListener = null
            } else {
                val listener = TabListener(onTabSelected)
                tabListener = listener
                tabLayout.addOnTabSelectedListener(listener)
            }
        }

        private inner class TabListener(
            private val onTabSelected: (Int) -> Unit
        ) : TabLayout.BaseOnTabSelectedListener<TabLayout.Tab> {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                onTabSelected(tab.position)
            }
        }
    }
}

data class RequestDetailsTabModel(
    val title1: String,
    val title2: String,
    val selected: Int
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
                isNestedScrollingEnabled = false
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
