package com.quincysx.crypto.bip44;

import com.quincysx.crypto.ECKeyPair;
import com.quincysx.crypto.bip32.ExtendedKey;
import com.quincysx.crypto.bip32.Index;
import com.quincysx.crypto.bip32.ValidationException;
import com.quincysx.crypto.bitcoin.BCHCoinECKeyPair;
import com.quincysx.crypto.bitcoin.BitCoinECKeyPair;
import com.quincysx.crypto.bitcoin.DogeCoinECKeyPair;
import com.quincysx.crypto.bitcoin.LtcCoinECKeyPair;
import com.quincysx.crypto.eos.EOSECKeyPair;
import com.quincysx.crypto.ethereum.EthECKeyPair;


/**
 * @author QuincySx
 * @date 2018/3/5 下午3:48
 */
public class CoinPairDerive {
    private ExtendedKey mExtendedKey;

    public CoinPairDerive(ExtendedKey extendedKey) {
        mExtendedKey = extendedKey;
    }

    public ExtendedKey deriveByExtendedKey(AddressIndex addressIndex) throws ValidationException {
        int address = addressIndex.getValue();
        int change = addressIndex.getParent().getValue();
        int account = addressIndex.getParent().getParent().getValue();
        CoinEnum coinType = addressIndex.getParent().getParent().getParent().getValue();
        int purpose = addressIndex.getParent().getParent().getParent().getParent().getValue();


        ExtendedKey child = mExtendedKey
                .getChild(Index.hard(purpose))
                .getChild(Index.hard(coinType.coinType()))
                .getChild(Index.hard(account))
                .getChild(change)
                .getChild(address);
        return child;
    }

    public ECKeyPair derive(AddressIndex addressIndex) throws ValidationException {
        CoinEnum coinType = addressIndex.getParent().getParent().getParent().getValue();
        ExtendedKey child = deriveByExtendedKey(addressIndex);
        ECKeyPair ecKeyPair = convertKeyPair(child, coinType);
        return ecKeyPair;
    }

    public ECKeyPair convertKeyPair(ExtendedKey child, CoinEnum coinType) throws ValidationException {
        switch (coinType) {
            case BitcoinTest:
                return BitCoinECKeyPair.parse(child.getMaster(), true);// convertBitcoinKeyPair(new BigInteger(1, child.getMaster().getPrivate()), true);
            case Ethereum:
            case TRX:
                return EthECKeyPair.parse(child.getMaster());//convertEthKeyPair(new BigInteger(1, child.getMaster().getPrivate()));
            case EOS:
                return EOSECKeyPair.parse(child.getMaster());
            case Litecoin:
                return LtcCoinECKeyPair.parse(child.getMaster(), false);
            case Dogecoin:
                return DogeCoinECKeyPair.parse(child.getMaster(), false);
            case BCH:
                return BCHCoinECKeyPair.parse(child.getMaster(), false);
            case XRP:
            case Bitcoin:
            default:
                return BitCoinECKeyPair.parse(child.getMaster(), false);//convertBitcoinKeyPair(new BigInteger(1, child.getMaster().getPrivate()), false);
        }
    }
}
