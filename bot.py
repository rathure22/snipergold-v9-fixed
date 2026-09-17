import time
import ccxt

# ==========================================
# 🔌 WIRING SECTION (BROKER CONFIG)
# ==========================================
BINANCE_API_KEY = ''
BINANCE_API_SECRET = ''

# ==========================================
# SENSITIVITY UG SIGNAL LOGIC
# ==========================================
class Sensitivity:
    LOW = "Low"
    NORMAL = "Normal"
    STRICT = "Strict"

def evaluate_trade_signal(rsi, is_uptrend, sensitivity):
    if sensitivity == Sensitivity.STRICT:
        if is_uptrend and rsi <= 30.0: return "BUY 🟢"
        elif not is_uptrend and rsi >= 70.0: return "SELL 🔴"
        else: return "WAIT ⚪"
    elif sensitivity == Sensitivity.NORMAL:
        if is_uptrend and rsi <= 45.0: return "BUY 🟢"
        elif not is_uptrend and rsi >= 55.0: return "SELL 🔴"
        else: return "WAIT ⚪"
    elif sensitivity == Sensitivity.LOW:
        return "BUY 🟢" if is_uptrend else "SELL 🔴"

def calculate_rsi(closes, period=14):
    if len(closes) < period + 1: return 50.0
    gains = []
    losses = []
    for i in range(1, len(closes)):
        change = closes[i] - closes[i-1]
        if change > 0:
            gains.append(change)
            losses.append(0)
        else:
            gains.append(0)
            losses.append(abs(change))
            
    avg_gain = sum(gains[-period:]) / period
    avg_loss = sum(losses[-period:]) / period
    
    if avg_loss == 0: return 100.0
    rs = avg_gain / avg_loss
    return 100 - (100 / (1 + rs))

# ==========================================
# MAIN BOT LOOP
# ==========================================
def main():
    print("=======================================")
    print("      SNIPERGOLD V9 - BINANCE          ")
    print("=======================================")
    
    print("[!] Nagkonektar sa Binance...")
    broker = ccxt.binance({
        'apiKey': BINANCE_API_KEY,
        'secret': BINANCE_API_SECRET,
        'enableRateLimit': True,
    })
    symbol = 'PAXG/USDT'
    
    # SENSITIVITY SETUP
    print("\nPilia ang Sensitivity Level:")
    print(" 1. Strict")
    print(" 2. Normal")
    print(" 3. Low")
    sens_choice = input("I-type ang numero (1/2/3): ")
    
    if sens_choice == '1': current_sens = Sensitivity.STRICT
    elif sens_choice == '2': current_sens = Sensitivity.NORMAL
    elif sens_choice == '3': current_sens = Sensitivity.LOW
    else: current_sens = Sensitivity.STRICT

    print(f"\n[!] Bot andam na! Sensitivity: {current_sens} | Asset: {symbol}\n")

    try:
        while True:
            candles = broker.fetch_ohlcv(symbol, '1m', limit=20)
            closes = [candle[4] for candle in candles]

            if len(closes) > 2:
                current_price = closes[-1]
                prev_price = closes[-2]
                
                is_uptrend = current_price > prev_price
                rsi = calculate_rsi(closes)
                
                trend_str = "Uptrend  " if is_uptrend else "Downtrend"
                signal = evaluate_trade_signal(rsi, is_uptrend, current_sens)
                
                print(f"Pair: {symbol} | Presyo: ${current_price:.2f} | RSI: {rsi:5.2f} | Trend: {trend_str} ==> ACTION: {signal}")
            
            time.sleep(5)
            
    except Exception as e:
        print(f"\n[X] Naay Error: {e}")
    except KeyboardInterrupt:
        print("\n\n[!] Bot malampusong gihunong.")

if __name__ == "__main__":
    main()

