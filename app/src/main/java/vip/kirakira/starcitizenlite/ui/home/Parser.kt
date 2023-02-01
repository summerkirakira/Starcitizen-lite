package vip.kirakira.starcitizenlite.ui.home

class Parser {
    companion object {
        fun getUpgradeOriginalName(upgradeTitle: String): List<String> {
            val nameList = mutableListOf<String>()
            for(shipName in upgradeTitle.replace("Upgrade - ", "").split(" to ")){
                nameList.add(shipName.trim())
            }
            return nameList
        }
    }
}