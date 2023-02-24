package th.co.dv.p2p.common.utilities.manager

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MockCreditNoteManager : BaseCreditNoteManager<MockCreditNote, MockCreditNoteItem>() {

    override var className: String = MockCreditNoteManager::class.java.simpleName
    override var logger: Logger = LoggerFactory.getLogger(MockCreditNoteManager::class.java)
    override var headerClass = MockCreditNote::class.java
    override var itemClass = MockCreditNoteItem::class.java

    var creditNoteService: MockCreditNoteService = MockCreditNoteService()

    override fun initQueryableService(): MockCreditNoteService {
        return creditNoteService
    }

}