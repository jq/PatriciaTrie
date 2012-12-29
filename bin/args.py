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
        default="localhost",
        required=False)

    parser.add_argument("-port",
        type=int,
        help="Machine's port",
        dest='port',
        default=8666,
        required=False)

    parser.add_argument("-core",
        type=str,
        help="Core's name",
        dest='core',
        default=None,
        required=False)

    parser.add_argument("strings",
        type=str,
        help="Strings to add",
        nargs='*')

    pp = parser.parse_args(sys.argv[1:])

    core_path = "/"
    if pp.core:
        core_path = "/%s/" % pp.core

    pp.url = "http://%s:%s%sapi/" % (pp.host, pp.port, core_path)

    return pp