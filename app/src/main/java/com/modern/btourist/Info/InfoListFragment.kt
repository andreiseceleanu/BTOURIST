package com.modern.btourist.Info


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.navigation.findNavController

import com.modern.btourist.R

class InfoListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_info_list, container, false)

        var infoNameList: ArrayList<String> = ArrayList()
        var infoList: ArrayList<String> = ArrayList()

        infoNameList.add(0,"Geography")
        infoList.add(0,"The city has an area of 226 km2 (87 sq mi). The altitude varies from 55.8 m (183.1 ft) at the Dâmbovița bridge in Cățelu, southeastern Bucharest and 91.5 m (300.2 ft) at the Militari church. The city has a roughly round shape, with the centre situated in the cross-way of the main north-south/east-west axes at University Square.\n\n The milestone for Romania's Kilometre Zero is placed just south of University Square in front of the New St. George Church (Sfântul Gheorghe Nou) at St. George Square (Piața Sfântul Gheorghe). Bucharest's radius, from University Square to the city limits in all directions, varies from 10 to 12 km (6 to 7 mi). ")

        infoNameList.add(1,"Climate")
        infoList.add(1,"Bucharest has a humid continental climate (Dfa), with warm to hot, humid summers and cold, snowy winters. Owing to its position on the Romanian Plain, the city's winters can get windy, though some of the winds are mitigated due to urbanisation. Winter temperatures often dip below 0 °C (32 °F), sometimes even to −20 °C (−4 °F). In summer, the average temperature is 23 °C (73 °F) (the average for July and August).\n\n Temperatures frequently reach 35 to 40 °C (95 to 104 °F) in midsummer in the city centre. Although average precipitation and humidity during summer are low, occasional heavy storms occur. During spring and autumn, daytime temperatures vary between 17 and 22 °C (63 and 72 °F), and precipitation during spring tends to be higher than in summer, with more frequent yet milder periods of rain. ")

        infoNameList.add(2,"Administration")
        infoList.add(2,"Bucharest has a unique status in Romanian administration, since it is the only municipal area that is not part of a county. Its population, however, is larger than that of any other Romanian county, hence the power of the Bucharest General Municipality (Primăria Generală), which is the capital's local government body, is the same as any other Romanian county council. \n\n" +
                "\n\nThe Municipality of Bucharest, along with the surrounding Ilfov County, is part of the București – Ilfov development region project, which is equivalent to NUTS-II regions in the European Union and is used both by the EU and the Romanian government for statistical analysis, and to co-ordinate regional development projects and manage funds from the EU. The Bucharest-Ilfov development region is not, however, an administrative entity yet. ")

        infoNameList.add(3,"Safety")
        infoList.add(3,"Bucharest's crime rate is rather low in comparison to other European capital cities, with the number of total offenses declining by 51% between 2000 and 2004, and by 7% between 2012 and 2013. The violent crime rate in Bucharest remains very low, with 11 murders and 983 other violent offenses taking place in 2007. Although violent crimes fell by 13% in 2013 compared to 2012, 19 murders (18 of which the suspects were arrested) were recorded. \n\n" +
                "\nAlthough in the 2000s, a number of police crackdowns on organized crime gangs occurred, such as the Cămătaru clan, organized crime generally has little impact on public life. Petty crime, however, is more common, particularly in the form of pickpocketing, which occurs mainly on the city's public transport network. Confidence tricks were common in the 1990s, especially in regards to tourists, but the frequency of these incidents has since declined. \n\nHowever, in general, theft was reduced by 13.6% in 2013 compared to 2012 Levels of crime are higher in the southern districts of the city, particularly in Ferentari, a socially disadvantaged area. ")


        infoNameList.add(4, "Money")
        infoList.add(4,"Though part of the European Union, Romania has not yet adopted the sole currency of the Union. Getting familiar with the national currency of Romania before actually traveling to Bucharest is advisable, in order not to become a victim of possible deceits.\n\n" +
                "\nThe national currency of Romania is the leu (plural, lei), with subunits called ban (plural, bani) (1 leu consisting of 100 bani). Notes refer to banknotes of 1, 5, 10, 50, 100, 200 and 500 lei, whereas coins refer to 1, 5, 10 and 50 bani.")

        infoNameList.add(5,"Custom Regulations")
        infoList.add(5,"Romania is not exempt from the customs regulations enforced in the European Union. Thus, tourists are not allowed to bring, for instance, more than 200 cigarettes and 2 litters of liquor. As a particular feature, the border checkpoints (for tourists who travel by bus or by car) can get quite crowded during the summer season or during the traditional religious holidays (such as the winter holidays). \nBy comparison, this is yet another feature which should determine tourists to choose flights over any other means of getting to Bucharest.")

        infoNameList.add(6,"Documents")
        infoList.add(6,"In order to enter Romania, European Union citizens must present a valid identity card. Travelers who come to Romania from outside the European Union must hold a valid passport.")

        infoNameList.add(7,"Timezone")
        infoList.add(7,"2 hours ahead of GMT (summer time)")

        infoNameList.add(8,"Water")
        infoList.add(8,"Though drinkable, tap water should be ruled out in favor of bottled water. Usually, most locals do not complain about the quality of tap water, but bottled water is universally safer to drink. ")

        infoNameList.add(9,"Emergency Number")
        infoList.add(9,"112\n" +
                "This is a toll-free, general number & can be dialed from any phone, even when locked.")

        infoNameList.add(10,"Language")
        infoList.add(10,"According to the 2002 Romanian Census, Romanian is spoken by 91% of the population as a primary language. According to the Romanian Constitution  and the law 1206 of 2006 the official language in Romania is Romanian both at the national and local level. ")

        infoNameList.add(11,"Public Transport")
        infoList.add(11,"Bucharest's public transport system is the largest in Romania and one of the largest in Europe.\n\n It is made up of the Bucharest Metro, run by Metrorex, as well as a surface transport system run by STB (Societatea de Transport București, previously known as the RATB), which consists of buses, trams, trolleybuses, and light rail. In addition, a private minibus system operates there. As of 2007, a limit of 10,000 taxicab licenses was imposed.")

        var adapter = ArrayAdapter(context,android.R.layout.simple_list_item_1,infoNameList)
        var listView = view.findViewById<ListView>(R.id.infoListView)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->

            view.findNavController().navigate(InfoListFragmentDirections.actionInfoListFragmentToInfoFragment(infoList.toTypedArray(),position))

        }

        return view
    }


}
