package th.co.dv.p2p.common.utilities.manager

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MockPurchaseItemManager : BasePurchaseItemManager<MockPurchaseItem>() {

    override var logger: Logger = LoggerFactory.getLogger(MockPurchaseItemManager::class.java)
    override var className: String = MockPurchaseItemManager::class.java.simpleName

    override var itemClass = MockPurchaseItem::class.java
    val purchaseItemService: MockPurchaseItemService = MockPurchaseItemService()

    override fun initQueryableService(): QueryableService<MockPurchaseItem> {
        return purchaseItemService
    }
}