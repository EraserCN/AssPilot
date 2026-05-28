import requests
import uuid
import json
import sys

# ================= 核心配置区 =================
BASE_URL = "https://mindpilot-server-sg.allawnos.com"
# ⚠️ 必填：请替换为浏览器中的真实 Cookie
YOUR_COOKIE = "YOUR_ACTUAL_COOKIE_STRING"

DEFAULT_MODEL = "Mind Pilot"
AVAILABLE_MODELS = ["Mind Pilot", "Gemini", "Perplexity", "ChatGPT", "Claude"]

HEADERS = {
    "app-version-id": "20260306",
    "Content-Type": "application/json",
    "Cookie": YOUR_COOKIE,
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}

# 终端颜色
COLOR_USER = "\033[94m"
COLOR_AI = "\033[92m"
COLOR_SYS = "\033[93m"
COLOR_ERROR = "\033[91m"
COLOR_RESET = "\033[0m"

# 状态全局变量
current_model = DEFAULT_MODEL
current_lang = "zh"

# ================= 国际化字典 (I18N) =================
I18N = {
    "zh": {
        "welcome": "🚀 Mind Pilot 多模型终端助手 (双语增强版)",
        "hint": "[*] 命令: '/model' 切换模型 | '/lang' 切换语言 | '/exit' 退出",
        "you": "你",
        "model_list": "\n--- 可选模型列表 ---",
        "choose_num": "请选择编号: ",
        "switched_model": "[+] 已切换模型至: ",
        "invalid_input": "[!] 无效输入，请重试",
        "lang_switched": "[+] 系统及对话语言已切换为: 中文 (Chinese)",
        "req_fail": "[!] 请求失败",
        "exception": "[!] 网络或解析异常: ",
        "exit": "\n[*] 正在安全退出程序，再见！"
    },
    "en": {
        "welcome": "🚀 Mind Pilot Multi-Model Terminal (Bilingual Edition)",
        "hint": "[*] Commands: '/model' switch model | '/lang' toggle language | '/exit' quit",
        "you": "You",
        "model_list": "\n--- Available Models ---",
        "choose_num": "Select number: ",
        "switched_model": "[+] Model switched to: ",
        "invalid_input": "[!] Invalid input, please try again",
        "lang_switched": "[+] System and chat language switched to: English",
        "req_fail": "[!] Request failed",
        "exception": "[!] Network or parsing exception: ",
        "exit": "\n[*] Exiting program gracefully. Goodbye!"
    }
}


# 翻译辅助函数
def t(key):
    return I18N[current_lang].get(key, key)


def print_sys(msg): print(f"{COLOR_SYS}{msg}{COLOR_RESET}")


def print_error(msg): print(f"{COLOR_ERROR}{msg}{COLOR_RESET}")


# ================= 交互功能函数 =================
def switch_lang():
    global current_lang
    # 中英语言反转逻辑
    current_lang = "en" if current_lang == "zh" else "zh"
    print_sys(t("lang_switched"))


def switch_model():
    global current_model
    print_sys(t("model_list"))
    for i, m in enumerate(AVAILABLE_MODELS):
        print_sys(f"  [{i}] {m}")

    choice = input(f"{COLOR_SYS}{t('choose_num')}{COLOR_RESET}").strip()
    try:
        idx = int(choice)
        if 0 <= idx < len(AVAILABLE_MODELS):
            current_model = AVAILABLE_MODELS[idx]
            print_sys(f"{t('switched_model')}{current_model}\n")
        else:
            print_error(t("invalid_input"))
    except ValueError:
        print_error(t("invalid_input"))


# ================= 核心请求逻辑 =================
def ask_mind_pilot(user_input, session_id):
    global current_model, current_lang
    message_id = str(uuid.uuid4())

    payload = {
        "contents": [
            {
                "type": "text",
                "data": user_input,
                "mime_type": "text/plain"
            }
        ],
        "message": user_input,
        "model": current_model,
        "session_id": session_id,
        "message_id": message_id,
        "history": [],
        "stream": True,
        "route": {
            "model": current_model,
            "strategy": "default",
            "session_id": session_id,
            "recommended_models": [current_model],
            "user_specified": True  # 强声明用户指定模型
        },
        "meta": {
            "language": current_lang,  # 动态读取当前语言环境 (zh/en)
            "timezone": "Asia/Shanghai"
        }
    }

    try:
        # Preprocess
        requests.post(f"{BASE_URL}/assistant/api/v1/preprocess", headers=HEADERS, json=payload, timeout=10)

        # Chat
        chat_resp = requests.post(f"{BASE_URL}/assistant/api/v1/chat", headers=HEADERS, json=payload, stream=True,
                                  timeout=30)

        if chat_resp.status_code != 200:
            print_error(f"\n{t('req_fail')} ({chat_resp.status_code}): {chat_resp.text}")
            return

        # 打印响应文本
        print(f"{COLOR_AI}{current_model}: {COLOR_RESET}", end="")
        for line in chat_resp.iter_lines():
            if line:
                decoded = line.decode('utf-8')
                if decoded.startswith("data: "):
                    try:
                        data = json.loads(decoded[6:])
                        if "choices" in data:
                            content = data["choices"][0].get("delta", {}).get("content", "")
                            print(f"{COLOR_AI}{content}{COLOR_RESET}", end="", flush=True)
                    except json.JSONDecodeError:
                        pass
        print("\n")
    except Exception as e:
        print_error(f"\n{t('exception')}{e}")


# ================= 程序主入口 =================
def main():
    print_sys("==================================================")
    print_sys(t("welcome"))
    print_sys(t("hint"))
    print_sys("==================================================\n")

    session_id = str(uuid.uuid4())

    while True:
        try:
            # 动态调整前缀：如 You [Gemini] / 你 [Gemini]
            prompt_str = f"{COLOR_USER}{t('you')} [{current_model}]: {COLOR_RESET}"
            user_input = input(prompt_str).strip()

            if not user_input: continue

            # 命令路由
            cmd = user_input.lower()
            if cmd in ['/exit', 'quit']:
                print_sys(t("exit"))
                break
            if cmd == '/model':
                switch_model()
                continue
            if cmd == '/lang':
                switch_lang()
                continue

            ask_mind_pilot(user_input, session_id)

        except KeyboardInterrupt:
            print_sys(t("exit"))
            sys.exit(0)


if __name__ == "__main__":
    main()