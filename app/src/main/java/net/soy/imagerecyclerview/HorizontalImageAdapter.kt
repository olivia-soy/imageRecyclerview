package net.soy.imagerecyclerview

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import net.soy.imagerecyclerview.base.BaseAdapter
import net.soy.imagerecyclerview.base.BaseItem
import net.soy.imagerecyclerview.base.BaseViewHolder
import net.soy.imagerecyclerview.base.TextItem

class HorizontalImageAdapter(var activity: Activity, context: Context) : BaseAdapter<BaseItem>(context) {

    companion object {
        private val TAG = HorizontalImageAdapter::class.java.simpleName
        const val VIEW_IMAGE_ADD: Int = 1000
        const val VIEW_IMAGE: Int = 1001
    }

    override fun onCreateCustomholder(parent: ViewGroup, viewType: Int): BaseViewHolder<*>? {
        return when (viewType) {
            VIEW_IMAGE_ADD -> ImageAddViewHolder(
                this, LayoutInflater.from(parent.context).inflate(
                    R.layout.item_image_add, parent, false
                )
            )
            VIEW_IMAGE -> ImageViewHolder(
                this,
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_image_delete,
                    parent,
                    false
                )
            )
            else -> null
        }
    }

    fun init() {
        Log.w(TAG, "init()")
        clear()
        addItem(BaseItem(VIEW_IMAGE_ADD))
        notifyDataSetChanged()
    }

    fun addImage(imgUrl: String?) {
        Log.w(TAG, "addImage(), imgUrl : $imgUrl")
        addItem(TextItem(VIEW_IMAGE, imgUrl))
        notifyDataSetChanged()
    }

    fun removeImage(position: Int) {
        Log.w(TAG, "removeItem(), position : $position")
        removeItem(position)
        notifyDataSetChanged()
    }


    class ImageAddViewHolder(adapter: BaseAdapter<*>, itemView: View) :
        BaseViewHolder<BaseItem>(adapter, itemView) {
        private var ivImage: ImageView = itemView.findViewById(R.id.iv_image)
        private var mActivity = (adapter is HorizontalImageAdapter).run{(adapter as HorizontalImageAdapter).activity}

        override fun onBindView(item: BaseItem?, position: Int) {
            super.onBindView(item, position)

            ivImage.setOnClickListener {
                if (mActivity is MainActivity) {
                    (mActivity as MainActivity).apply {
                        mIsHorizontal = true
                        showSelectAlert()
                    }
                }
            }

        }
    }

    class ImageViewHolder(adapter: BaseAdapter<*>, itemView: View) :
        BaseViewHolder<TextItem>(adapter, itemView) {
        private var ivImage: ImageView = itemView.findViewById(R.id.iv_image)
        private var rltDelete: RelativeLayout = itemView.findViewById(R.id.rlt_delete)
        private var mActivity = (adapter is HorizontalImageAdapter).run{(adapter as HorizontalImageAdapter).activity}
        override fun onBindView(item: TextItem?, position: Int) {
            super.onBindView(item, position)
            context?.let {
                Glide.with(it).load(item?.imageUrl).into(ivImage)
            }
            rltDelete.setOnClickListener {
                if (mActivity is MainActivity) {
                    (mActivity as MainActivity).horizontalImageAdapter?.removeImage(position)
                }
            }
        }
    }
}
