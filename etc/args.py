import argparse
import sys

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
    nargs='+')

pp = parser.parse_args(sys.argv[1:])
if not pp.host.startsWith("http://") or not pp.host.startsWith("https://"):
    pp.host = "http://%s" % pp.host