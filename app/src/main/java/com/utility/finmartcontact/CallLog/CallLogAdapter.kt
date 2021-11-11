package com.utility.finmartcontact.CallLog

//import kotlinx.android.synthetic.main.layout.call_log_item.xml.view.*
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.utility.finmartcontact.R
import com.utility.finmartcontact.core.model.CallLogEntity

class CallLogAdapter(val callLogList: MutableList<CallLogEntity>,  val context: Context) :
    RecyclerView.Adapter<CallLogAdapter.CallLogItem>() {




    class  CallLogItem(itemView: View) : RecyclerView.ViewHolder(itemView){



        val txt1 = itemView.findViewById(R.id.txt1) as TextView
        val txt2 = itemView.findViewById(R.id.txt2) as TextView
        val txt3 = itemView.findViewById(R.id.txt3) as TextView
        val txt4 = itemView.findViewById(R.id.txt4) as TextView
        val txt5 = itemView.findViewById(R.id.txt5) as TextView

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallLogItem {
        return CallLogItem(LayoutInflater.from(context).inflate(
            R.layout.call_log_item
            , parent, false))
    }

    override fun getItemCount(): Int {


        return callLogList.size
    }

    override fun onBindViewHolder(holder: CallLogItem, position: Int) {
                var callLogItem = callLogList.get(position)
//        holder.txtAddress.setText(fbaItem.City + "(" + fbaItem.Zone + "), " + fbaItem.statename + "-" + fbaItem.Pincode)


        holder.txt1.setText(callLogItem.name)
        holder.txt2.setText(callLogItem.mobileno)
        holder.txt3.setText(callLogItem.callType)
        holder.txt4.setText(secToTime(callLogItem.callDuration))
        holder.txt5.setText(callLogItem.callDate)
    }


    fun secToTime(secTemp: String): String? {

       val  sec = secTemp.toIntOrNull()
           var seconds = sec?.rem(60);
           var minutes = sec?.div(60);
        if (minutes != null) {
            if (minutes >= 60) {
                val hours = minutes / 60;
                minutes %= 60;
                if( hours >= 24) {
                    var days = hours / 24;
                    return String.format("%d days %02d:%02d:%02d", days,hours%24, minutes, seconds);
                }
                return String.format("%02d:%02d:%02d", hours, minutes, seconds);
            }
        }
           return String.format("00:%02d:%02d", minutes, seconds);
       }



}