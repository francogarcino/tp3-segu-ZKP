import sys
import requests
import getpass
import secrets

BASE_URL = "http://localhost:8080/auth"
BASE_URL_TEST = "http://localhost:8080/auth/test"
ACTIVE_URL = BASE_URL

# --- NEW: Privacy mode ---
PRIVACY_MODE = True   # default: hide secrets


def read_secret(prompt: str) -> int:
    """
    Reads the secret x. If privacy mode is ON, hide the input.
    If privacy OFF, show plaintext input.
    """
    if PRIVACY_MODE:
        raw = getpass.getpass(prompt).strip()
    else:
        raw = input(prompt).strip()

    try:
        return int(raw)
    except ValueError:
        print("Secret must be an integer.")
        return None


def fetch_vars():
    r = requests.get(f"{ACTIVE_URL}/vars")
    r.raise_for_status()
    return r.json()


def do_register():
    print("=== REGISTER ===")

    username = ""
    while username.strip() == "":
        username = input("Username: ").strip()
        if username == "":
            print("Username cannot be empty.")

    x = read_secret("Secret x: ")
    if x is None:
        return

    try:
        vars_json = fetch_vars()
        p = int(vars_json["primeModulusP"])
        g = int(vars_json["generatorG"])
    except Exception as e:
        print("Error fetching vars:", e)
        return

    v = pow(g, x, p)
    v_hex = format(v, "x")

    body = {
        "username": username,
        "verifierHex": v_hex
    }

    try:
        resp = requests.post(f"{ACTIVE_URL}/register", json=body)
        resp_json = resp.json()
        print("REGISTER RESPONSE:", resp_json)
    except Exception as e:
        print("Error sending register request:", e)


def do_login(force_fail: bool):
    print("=== LOGIN{} ===".format(" (FORCED FAIL)" if force_fail else ""))

    username = ""
    while username.strip() == "":
        username = input("Username: ").strip()
        if username == "":
            print("Username cannot be empty.")

    x = read_secret("Secret x: ")
    if x is None:
        return

    try:
        vars_json = fetch_vars()
        p = int(vars_json["primeModulusP"])
        q = int(vars_json["subgroupOrderQ"])
        g = int(vars_json["generatorG"])
    except Exception as e:
        print("Error fetching vars:", e)
        return

    r_val = secrets.randbelow(q - 1) + 1
    t = pow(g, r_val, p)
    t_hex = format(t, "x")

    start_body = {
        "username": username,
        "commitmentHex": t_hex
    }

    try:
        start_resp = requests.post(f"{ACTIVE_URL}/start", json=start_body)
        start_resp.raise_for_status()
        start_json = start_resp.json()
    except Exception as e:
        print("Error sending start request:", e)
        return

    c_hex = start_json.get("challengeHex", "")
    if c_hex == "":
        print("Server returned empty challenge (unknown user?)")
        return

    try:
        c = int(c_hex, 16)
    except ValueError:
        c = int(c_hex)

    s = (r_val + c * x) % q
    if force_fail:
        s = (s + 1) % q

    s_hex = format(s, "x")

    finish_body = {
        "username": username,
        "responseHex": s_hex,
        "commitmentHex": t_hex
    }

    try:
        finish_resp = requests.post(f"{ACTIVE_URL}/finish", json=finish_body)
        finish_resp.raise_for_status()
        finish_json = finish_resp.json()
    except Exception as e:
        print("Error sending finish request:", e)
        return

    print("FINISH RESPONSE:", finish_json)


def repl():
    global ACTIVE_URL, PRIVACY_MODE

    print("ZKP Demo REPL")
    print("Commands:")
    print("  register")
    print("  login")
    print("  quit")
    print("-------------------------------")

    while True:
        try:
            cmd = input("> ").strip()
        except (EOFError, KeyboardInterrupt):
            print("\nBye!")
            sys.exit(0)

        if cmd == "quit":
            print("Bye!")
            sys.exit(0)

        elif cmd == "register":
            do_register()

        elif cmd == "login":
            do_login(force_fail=False)

        elif cmd == "login --f":
            do_login(force_fail=True)

        elif cmd.startswith("privacy"):
            parts = cmd.split()
            if len(parts) != 2:
                print("Usage: privacy true|false")
                continue

            flag = parts[1].lower()
            if flag == "true":
                PRIVACY_MODE = True
                print("Privacy mode ON (secrets hidden)")
            elif flag == "false":
                PRIVACY_MODE = False
                print("Privacy mode OFF (secrets visible)")
            else:
                print("Value must be true or false")

        elif cmd.startswith("setTest"):
            parts = cmd.split()
            if len(parts) != 2:
                print("Usage: setTest true|false")
                continue

            flag = parts[1].lower()
            if flag == "true":
                ACTIVE_URL = BASE_URL_TEST
                print("Using TEST URL:", ACTIVE_URL)
            elif flag == "false":
                ACTIVE_URL = BASE_URL
                print("Using NORMAL URL:", ACTIVE_URL)
            else:
                print("Value must be true or false")

        elif cmd == "":
            continue

        else:
            print("Unknown command:", cmd)


if __name__ == "__main__":
    repl()
