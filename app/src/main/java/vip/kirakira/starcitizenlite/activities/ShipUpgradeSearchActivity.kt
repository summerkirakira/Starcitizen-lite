package vip.kirakira.starcitizenlite.activities
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import vip.kirakira.starcitizenlite.R
import vip.kirakira.starcitizenlite.ui.shipupgrade.ShipUpgradeCart


class ShipUpgradeSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ship_upgrade_search)
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            val fragment = ShipUpgradeCart()
            transaction.add(R.id.ship_upgrade_search_fragment_container, fragment)
            transaction.commit()
        }
    }
}