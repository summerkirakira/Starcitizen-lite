package vip.kirakira.starcitizenlite.ui.home

class Parser {
    companion object {
        fun getUpgradeOriginalName(upgradeTitle: String): List<UpgradeShipName> {
            val nameList = mutableListOf<UpgradeShipName>()
            for(shipName in upgradeTitle.replace("Upgrade - ", "").split(" to ")){
                val shipInfo = getFormattedShipName(shipName)
                if(shipInfo.contains("Standard Edition")) {
                    nameList.add(
                        UpgradeShipName(
                            name = shipInfo.replace("Standard Edition", "").trim(),
                            version = "Standard Edition"
                        )
                    )
                } else if(shipInfo.contains("BIS Warbond Edition")) {
                    nameList.add(
                        UpgradeShipName(
                            name = shipInfo.replace("BIS Warbond Edition", "").trim(),
                            version = "BIS Warbond Edition"
                        )
                    )
                } else if(shipInfo.contains("Warbond Edition")) {
                    nameList.add(
                        UpgradeShipName(
                            name = shipInfo.replace("Warbond Edition", "").trim(),
                            version = "Warbond Edition"
                        )
                    )
                } else {
                    nameList.add(
                        UpgradeShipName(
                            name = shipInfo,
                            version = "Standard Edition"
                        )
                    )
                }
            }
            return nameList
        }
        data class UpgradeShipName(val name: String, val version: String)

        fun getFormattedShipName(shipName: String): String {
            var newShipName = shipName
                .replace("Banu", "")
                .replace("Drake", "")
                .replace("Crusader", "")
                .replace("Argo", "")
                .replace("Esperia", "")
                .replace("Upgrade", "")
                .replace("CNOU", "")
                .replace("AEGIS", "")
                .replace("Mercury Star Runner", "Mercury")
                .replace("ORIGIN 600i Exploration Module", "600i Explorer")
                .replace("ORIGIN 600i Touring Module", "600i Touring")
                .replace("RSI", "")
                .replace("Anvil", "")
                .replace("Retaliator Base", "Retaliator")
                .replace("Mole Carbon Edition", "Mole")
                .replace("Genesis Starliner", "Genesis")
                .trim()
            return newShipName
        }
    }
}