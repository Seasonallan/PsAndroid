package com.ripple.bean.transaction;

import com.ripple.bean.base.BaseDao;
import com.ripple.bean.base.Json;

import java.io.Serializable;

/**
 * ""ModifiedNode": {
 *  *                                 "FinalFields": {
 *  *                                     "Account": "rNqKQoZzmYEXSafD2JU6pgNEp1BpJUU9oV",
 *  *                                     "Balance": "1000005000",
 *  *                                     "Flags": 0,
 *  *                                     "OwnerCount": 0,
 *  *                                     "Sequence": 3
 *  *                                 },
 *  *                                 "LedgerEntryType": "AccountRoot",
 *  *                                 "LedgerIndex": "3081229618A2822EF9BA1712BEDBCDDE34856373E99302F4FBE35881797E79D0",
 *  *                                 "PreviousFields": {
 *  *                                     "Balance": "1000004000"
 *  *                                 },
 *  *                                 "PreviousTxnID": "FF945C26164F83D8437699EB92002FC985A0432B7E4F6934BF5324A190E2CE0E",
 *  *                                 "PreviousTxnLgrSeq": 23
 *  *                             }
 */
public class XRPAccount_AffectedNode extends BaseDao implements Serializable {
    @Json(name = "")
    public XRPAccount_FinalFields FinalFields;
    @Json(name = "")
    public String LedgerEntryType;
    @Json(name = "")
    public String LedgerIndex;
    @Json(name = "")
    public XRPAccount_PreviousFields PreviousFields;
    @Json(name = "")
    public String PreviousTxnID;
    @Json(name = "")
    public int PreviousTxnLgrSeq;
}
