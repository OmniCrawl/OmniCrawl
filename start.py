#!/usr/bin/env python3
import subprocess
from sys import argv
from subprocess import Popen

null = subprocess.DEVNULL

def run_crawl(num_sites=0, checkpoint=False):
    subprocess.check_call('./start-xvfb.sh', shell=True)
    subprocess.check_call('./startremotecrawl.sh compile', shell=True)
    text= 'start'
    Popen('killall firefox firefox-bin geckodriver', shell=True).wait()
    proc = Popen(f'java -Xmx8G -Dlog4j.configurationFile=resources/log4j2.xml -jar target/Webdriver-1.0-SNAPSHOT-jar-with-dependencies.jar {num_sites} {checkpoint} 2>&1 | tee -a log/crawler.log', shell=True)
    proc.wait()
    try:
        with open('./log/checkpoint.txt') as f:
            text = "Crashed: progress " + f.read()
    except FileNotFoundError:
        text = "Checkpoint file not found"
    text += subprocess.check_output("cat log/crawler.log | grep 'Fatal' -i | tail",shell=True).decode().replace('"','').replace("'",'')


def get_args(args_schema, args_list):
    args = {}
    for arg in args_list:
        arg = arg.strip('--')
        if '=' in arg:
            parts = arg.split('=')
            key = parts[0]
            value = parts[1]
        else:
            key = arg
            value = None
        if key in args_schema:
            arg_type = args_schema[key][0]
            if arg_type == 'Int':
                args[key] = int(value)
            elif arg_type == 'Bool':
                args[key] = (True if value.lower() == 'true' else False)
            elif arg_type is None:
                args[key] = None
            else:
                raise Exception(f'Unhandled arg type {arg_type} for arg: {arg}')
        else:
            raise Exception(f'Unexpected argument {arg} | Valid arguments: {args_schema}')
    for key, value in args_schema.items():
        if key not in args:
            args[key] = value[1]
    if 'help' in args:
        print('Arguments:\nFlag | Type | Default | Description')
        for key, value in args_schema.items():
            print(f'--{key} | {value[0]} | {value[1]} | {value[2]}')
        raise Exception('Invalid arguments')
    return args


def main(args_list):
    args_schema = {
        'num-sites': ('Int', -1, 'The number of sites to use from the site list (-1 indicates use all)'),
        'checkpoint': ('Bool', False, 'Whether to use checkpointing (resume crawl from last index)')
    }
    args = get_args(args_schema, args_list)
    run_crawl(num_sites=args['num-sites'], checkpoint=args['checkpoint'])


if __name__ == '__main__':
    main(argv[1:])
