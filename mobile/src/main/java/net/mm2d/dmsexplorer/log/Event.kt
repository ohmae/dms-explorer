/*
 * Copyright (c) 2018 大前良介 (OHMAE Ryosuke)
 *
 * This software is released under the MIT License.
 * http://opensource.org/licenses/MIT
 */

package net.mm2d.dmsexplorer.log

/**
 * @author [大前良介 (OHMAE Ryosuke)](mailto:ryo@mm2d.net)
 */
internal object Event {
    const val ADD_PAYMENT_INFO = "add_payment_info"
    const val ADD_TO_CART = "add_to_cart"
    const val ADD_TO_WISHLIST = "add_to_wishlist"
    const val APP_OPEN = "app_open"
    const val BEGIN_CHECKOUT = "begin_checkout"
    const val CAMPAIGN_DETAILS = "campaign_details"
    const val ECOMMERCE_PURCHASE = "ecommerce_purchase"
    const val GENERATE_LEAD = "generate_lead"
    const val JOIN_GROUP = "join_group"
    const val LEVEL_END = "level_end"
    const val LEVEL_START = "level_start"
    const val LEVEL_UP = "level_up"
    const val LOGIN = "login"
    const val POST_SCORE = "post_score"
    const val PRESENT_OFFER = "present_offer"
    const val PURCHASE_REFUND = "purchase_refund"
    const val SEARCH = "search"
    const val SELECT_CONTENT = "select_content"
    const val SHARE = "share"
    const val SIGN_UP = "sign_up"
    const val SPEND_VIRTUAL_CURRENCY = "spend_virtual_currency"
    const val TUTORIAL_BEGIN = "tutorial_begin"
    const val TUTORIAL_COMPLETE = "tutorial_complete"
    const val UNLOCK_ACHIEVEMENT = "unlock_achievement"
    const val VIEW_ITEM = "view_item"
    const val VIEW_ITEM_LIST = "view_item_list"
    const val VIEW_SEARCH_RESULTS = "view_search_results"
    const val EARN_VIRTUAL_CURRENCY = "earn_virtual_currency"
    const val REMOVE_FROM_CART = "remove_from_cart"
    const val CHECKOUT_PROGRESS = "checkout_progress"
    const val SET_CHECKOUT_OPTION = "set_checkout_option"
}
