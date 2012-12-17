import argparse
import sys

def args():
    parser = argparse.ArgumentParser(
        add_help=False,
        description="Add a string")

    parser.add_argument("-host",
        type=str,
        help="Machine's hostname",
        dest='host',
        default="http://localhost:8666",
        required=False)

    parser.add_argument("keys",
        type=str,
        help="Keys to add",
        nargs='*')

    pp = parser.parse_args(sys.argv[1:])
    if not pp.host.startswith("http://") and not pp.host.startswith("https://"):
        pp.host = "http://%s" % pp.host

    pp.host = "%s/api/" % pp.host

    return pp