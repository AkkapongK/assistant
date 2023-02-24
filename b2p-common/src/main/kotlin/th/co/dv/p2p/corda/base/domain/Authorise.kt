package th.co.dv.p2p.corda.base.domain

object Auth {

    const val OR = " or "
    const val AND = " and "

    object ROOT {
        const val ADMIN: String = "hasAuthority('Admin')"
        const val INTERFACE = "hasRole('INTERFACE')"
        const val BUYER_CUSTOM_INTERFACE = "hasRole('BUYER_CUSTOM_INTERFACE')"
    }

    object PURCHASE {
        const val CREATE = "hasAuthority('PO-Upload')"
        const val VIEW = "hasAuthority('PO-List')"
        const val VIEW_DETAIL = "hasAuthority('PO-Detail')"
        const val CONFIRM_APPROVALS = "hasAuthority('PO-Confirm-Approval')"
        const val DELIVERY_VIEW = "hasAuthority('PO-Delivery-List')"
        const val EDIT_DELIVERY_DATE = "hasAuthority('PO-Edit-DeliveryDate')"
        const val COMFIRM_DELIVERY_DATE = "hasAuthority('PO-Confirm-DeliveryDate')"
        const val FINANCING = "hasAuthority('PO-Edit-POFinancing')"
        const val FINANCING_REPAYMENT = "hasAuthority('PO-POFinancing-Repayment')"
        const val DISABLE = "hasAuthority('PO-Disable')"
    }

    object INVOICE {
        const val EDIT = "hasAuthority('Invoice-Edit')"
        const val HOLD = "hasAuthority('Invoice-Hold')"
        const val UNHOLD = "hasAuthority('Invoice-Unhold')"
        const val CREATE = "hasAuthority('Invoice-Create')"
        const val UPLOAD = "hasAuthority('Invoice-Upload')"
        const val APPROVE_DOA = "hasAuthority('DOA-Approval')"
        const val APPROVE_3WM = "hasAuthority('3WM-Approval')"
        const val REJECT_INVOICE_AFTER_DOA = "hasAuthority('Invoice-Reject-After-DOA')"
        const val REFRESH_RETENTION = "hasAuthority('Invoice-Refresh-Retention')"
        const val EDIT_DUE_DATE = "hasAuthority('Invoice-Edit-PaymentDueDate')"
        const val VIEW = "hasAuthority('Invoice-List')"
        const val VIEW_DETAIL = "hasAuthority('Invoice-Detail')"
        const val VIEW_DOA = "hasAuthority('DOA-List')"
        const val VIEW_DOA_DETAIL = "hasAuthority('DOA-Detail')"
        const val VIEW_MATCHMAKER = "hasAuthority('3WM-List')"
        const val VIEW_MATCHMAKER_DETAIL = "hasAuthority('3WM-Detail')"
        const val CANCEL = "hasAuthority('Invoice-Cancel')"
        const val TAG_UNTAG_GOODS_RECEIVED = "hasAuthority('Invoice-Tag-Goods-Received')"
        const val REVIEW_EDIT = "hasAuthority('Review-Edit')"
        const val EDIT_FINANCING = "hasAuthority('Invoice-Edit-InvoiceFinancing')"
        const val DRAFTED_CONVERT = "hasAuthority('Invoice-Drafted-Convert')"
        const val FINAL_REVIEW_EDIT = "hasAuthority('Final-Review-Edit')"
        const val DOA_RESET = "hasAuthority('DOA-Reset')"
    }

    object GOODS {
        const val EDIT = "hasAuthority('GR-Edit')"
        const val CREATE = "hasAuthority('GR-Upload')"
        const val VIEW = "hasAuthority('GR-List')"
        const val VIEW_DETAIL = "hasAuthority('GR-Detail')"
        const val DELETE = "hasAuthority('GR-Delete')"
    }

    object CREDIT {
        const val CANCEL_QUANTITY = "hasAuthority('2WM-Approval')"
        const val CREATE = "hasAuthority('CN-Create')"
        const val CANCEL_PRICE = "hasAuthority('2WM-Approval')"
        const val VIEW = "hasAuthority('CN-List')"
        const val VIEW_2WM_MATCHMAKER = "hasAuthority('2WM-List')"
        const val VIEW_2WM_MATCHMAKER_DETAIL = "hasAuthority('2WM-Detail')"
        const val VIEW_DETAIL = "hasAuthority('CN-Detail')"
        const val APPROVE_2WM_QUANTITY = "hasAuthority('2WM-Approval')"
        const val APPROVE_PRICE = "hasAuthority('CN-Subsequent-Approval')"
        const val REJECT_2WM_QUANTITY = "hasAuthority('2WM-Approval')"
        const val REJECT_PRICE = "hasAuthority('CN-Subsequent-Approval')"
        const val EDIT_QUANTITY = "hasAuthority('CN-Edit')"
        const val EDIT_PRICE = "hasAuthority('CN-Edit')"
        const val TAG_UNTAG_GOODS_RECEIVED = "hasAuthority('CN-Tag-Goods-Received')"
        const val CN_EDIT_BUYER = "hasAuthority('CN-Edit-Buyer')"
        const val EDIT = "hasAuthority('CN-Edit')"
        const val CANCEL = "hasAuthority('CN-Cancel')"
        const val APPROVE_SUBSEQUENT = "hasAuthority('CN-Subsequent-Approval')"
        const val APPROVE_2WM = "hasAuthority('2WM-Approval')"
        const val APPROVE_OTHER = "hasAuthority('CN-Other-Approval')"
    }

    object REQUEST {
        const val CREATE = "hasAuthority('Request-Create')"
        const val SEND = "hasAuthority('Request-Send')"
        const val VIEW = "hasAuthority('Request-Detail')"
        const val APPROVE = "hasAuthority('Request-Approval')"
        const val EDIT = "hasAuthority('Request-Edit')"
        const val CANCEL = "hasAuthority('Request-Cancel')"
        const val CLOSE = "hasAuthority('Request-Close')"
        const val SELLER_APPROVE = "hasAuthority('Request-Receiver-Approval')"
        const val BUYER_APPROVE = "hasAuthority('Request-Requester-Approval')"
        const val SPLIT = "hasAuthority('Request-Retention-Split')"
        const val RE_SPLIT = "hasAuthority('Request-Retention-Re-Split')"
    }

    object PAYMENT {
        const val VIEW = "hasAuthority('MONITOR-Payment-List')"
        const val VIEW_DETAIL = "hasAuthority('MONITOR-Payment-Detail')"
        const val APPROVE_DOA = "hasAuthority('DOA-Payment-Approval')"
        const val REPOST = "hasAuthority('MONITOR-Payment-Repost')"
        const val PRINT_WHT_CERT = "hasAuthority('WHT-Cert-Print')"
        const val REJECT_REGEN = "hasAuthority('Payment-Management-Reject-Regen')"
    }

    object DEBIT {
        const val CREATE = "hasAuthority('DN-Create')"
        const val CANCEL = "hasAuthority('DN-Cancel')"
        const val EDIT = "hasAuthority('DN-Edit')"
        const val VIEW = "hasAuthority('DN-List')"
        const val VIEW_DETAIL = "hasAuthority('DN-Detail')"
        const val EXPORT = "hasAuthority('DN-Export')"
        const val APPROVE = "hasAuthority('DN-Approval')"
        const val REJECT_DEBIT_AFTER_DOA = "hasAuthority('Debit-Reject-After-DOA')"
        const val DOCUMENT_TRACKING_VIEW = "hasAuthority('Document-Tracking-List')"
        const val DOCUMENT_TRACKING_EDIT = "hasAuthority('Document-Tracking-Edit')"
        const val EDIT_DUE_DATE = "hasAuthority('DN-Edit-PaymentDueDate')"
        const val APPROVE_DOA = "hasAuthority('DOA-Approval')"
        const val DN_EDIT_BUYER = "hasAuthority('DN-Edit-Buyer')"
        const val DOA_RESET = "hasAuthority('DOA-Reset')"
        const val APPROVE_OTHER = "hasAuthority('DN-Other-Approval')"
        const val REVIEW_EDIT = "hasAuthority('Review-Edit')"
    }

    object LIV {
        const val REPOST = "hasAuthority('MONITOR-LIV-Repost')"
    }

    object TAXDOCUMENT {
        const val VIEW_DETAIL = "hasAuthority('TaxDocument-Detail')"
        const val CREATE = "hasAuthority('TaxDocument-Create')"
    }

    object RD {
        const val INVOICE_VIEW = "hasAuthority('Invoice-List-RD')"
        const val DEBIT_VIEW = "hasAuthority('DN-List-RD')"
        const val CREDIT_VIEW = "hasAuthority('CN-List-RD')"
        const val VIEW_DETAIL = "hasAuthority('TaxDocument-Detail-RD')"
        const val CHANGE_BUYER = "hasAuthority('Change-Buyer-RD')"
    }

    object PENDING_DOA {
        const val DASHBOARD_DOA_TO_DO_LIST = "hasAuthority('Dashboard-DOA-To-Do-List')"
        const val DASHBOARD_PENDING_DOA = "hasAuthority('Dashboard-Invoice-Pending-DOA')"
    }

    object MASS_ACTION {
        const val DOCUMENT_VIEW = "hasAuthority('Mass-Action-List')"
        const val EDIT_PAYMENT_DUE_DATE = "hasAuthority('Mass-Action-Edit-PaymentDueDate')"
        const val HOLD_DOCUMENTS = "hasAuthority('Mass-Action-Hold')"
        const val UNHOLD_DOCUMENTS = "hasAuthority('Mass-Action-Unhold')"
        const val MASS_ACTION_REJECT_AFTER_DOA = "hasAuthority('Mass-Action-Reject-After-DOA')"
    }

    object CONTRACT {
        const val VIEW = "hasAuthority('Contract-List')"
        const val EXPORT = "hasAuthority('Contract-Export')"
        const val CREATE = "hasAuthority('Contract-Upload')"
        const val VIEW_DETAIL = "hasAuthority('Contract-Detail')"
        const val EDIT = "hasAuthority('Contract-Edit')"
    }

    object SELF_CONFIG {
        const val USER_SELF_CONFIG_CONFIGURATION = "hasAuthority('User-Self-Config-Configuration')"
        const val USER_SELF_CONFIG_MANAGEVENDOR_EDIT_HOLDFLAG = "hasAuthority('User-Self-Config-ManageVendor-Edit-HoldFlag')"
        const val USER_SELF_CONFIG_MANAGEVENDOR_EDIT_ACTIVEFLAG = "hasAuthority('User-Self-Config-ManageVendor-Edit-ActiveFlag')"
        const val USER_SELF_CONFIG_UPLOAD_E_SIGNATURE = "hasAuthority('User-Self-Config-Upload-eSignature')"
    }

    object DASHBOARD {
        const val DASHBOARD_STATS = "hasAuthority('Dashboard-Stats')"
        const val DASHBOARD_INV_PENDING = "hasAuthority('Dashboard-INV-Pending')"
        const val DASHBOARD_INV = "hasAuthority('Dashboard-INV')"
        const val DASHBOARD_PAYMENTSTATS = "hasAuthority('Dashboard-PaymentStats')"
        const val DASHBOARD_3WM = "hasAuthority('Dashboard-3WM')"
        const val DASHBOARD_INV_PENDING_TAXDOC_REPORT = "hasAuthority('Dashboard-INV-Pending-TaxDoc-Report')"
    }

    object ADMIN {
        const val OPERATION_ACTION = "hasAuthority('Operation-Action')"
    }
}