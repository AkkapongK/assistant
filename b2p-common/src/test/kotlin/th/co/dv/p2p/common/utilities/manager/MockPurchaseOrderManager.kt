package th.co.dv.p2p.common.utilities.manager

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MockPurchaseOrderManager: BasePurchaseOrderManager<MockPurchaseOrder, MockPurchaseItem>() {


    override var logger: Logger = LoggerFactory.getLogger(MockPurchaseOrderManager::class.java)
    override var className: String = MockPurchaseOrderManager::class.java.simpleName
    override var headerClass = MockPurchaseOrder::class.java
    override var itemClass = MockPurchaseItem::class.java

     val purchaseOrderService: MockPurchaseOrderService = MockPurchaseOrderService()
     val itemManager: MockPurchaseItemManager = MockPurchaseItemManager()

    override fun initQueryableService(): MockPurchaseOrderService {
        return purchaseOrderService
    }

    override fun initItemsManager(): BasePurchaseItemManager<MockPurchaseItem> {
        return itemManager
    }

}