#!/usr/bin/python3

import click
import os.path
import hashlib
import subprocess 
import shutil
import time
import xml.etree.ElementTree as ET
from colorama import init, Fore, Back, Style

current_milli_time = lambda: int(round(time.time() * 1000))

def build_tinker_id():
    git_sha = subprocess.check_output(['git', 'rev-parse', 'HEAD'], stderr=subprocess.STDOUT).decode('ascii').strip()
    return "fish_{}_{}_patch".format(git_sha, current_milli_time())

def check_file_exists(file_name):
    abs_path = file_name
    if (file_name.startswith('~/')):
        abs_path = os.path.expanduser(file_name)
    else:
        abs_path = os.path.abspath(file_name)
    if os.path.exists(abs_path):
        return abs_path
    else:
        raise ValueError('{} not exists'.format(abs_path))

def parse_apk_info(apk):
    md5 = hashlib.md5()
    md5.update(os.path.basename(apk).encode('utf-8'))
    output = os.path.join(os.getcwd(), "build", md5.hexdigest())
    if not os.path.exists(output):
        # shutil.rmtree(output)
        command = ['apktool', 'd', apk, '-o', output]
        subprocess.check_call(command)
    manifest = ET.parse(os.path.join(output, "AndroidManifest.xml"))
    root = manifest.getroot()
    package = manifest.getroot().attrib['package']
    keys = []
    values = []
    meta_datas = {}
    for meta_data in root.find('application'):
        if (meta_data.tag != 'meta-data'):
            continue
        key = meta_data.attrib['{http://schemas.android.com/apk/res/android}name']
        value = meta_data.attrib['{http://schemas.android.com/apk/res/android}value']
        meta_datas[key] = value
    return {'package': package, 'tinker_id': meta_datas.get('TINKER_ID', 'N/A')}

def dump_apk_info(apk_info):
    print("APK DETAILS:")
    print('-------------------------------------------------------------------')
    print('{:15}{}'.format('PACKAGE', apk_info['package']))
    print('{:15}{}'.format('TINKER ID', apk_info['tinker_id']))
    print('-------------------------------------------------------------------')

@click.group()
def cli():
    pass

@click.command()
@click.option('--apk', help='base apk file for tinker patch')
@click.option('--symbol_file', help='resource mapping file')
@click.option('--proguard_file', help='proguard mapping file')
def patch(apk, symbol_file, proguard_file):
    # retrive apk information
    apk_file = check_file_exists(apk)
    apk_info = parse_apk_info(apk_file)
    dump_apk_info(apk_info)

    # make sure base apk has TINKERID
    if (apk_info['tinker_id'] == 'N/A'):
        raise Exception("Can't find tinker id in AndroidManifest.xml")

    commands = ['./gradlew', '-PenableTinker=true', '-PpatchMode=true']
    commands.append("-PbaseApk={}".format(apk_file))
    commands.append("-PbaseApkResourceMapping={}".format(check_file_exists(symbol_file)))
    commands.append("-PbaseApkProguardMapping={}".format(check_file_exists(proguard_file)))
    commands.append("-PtinkerId={}".format(build_tinker_id()))
    commands.append('clean')
    commands.append('buildTinkerPatchRelease')
    print("Starting to run following command:\n\n{}\n\n".format(' '.join(commands)))
    subprocess.check_call(commands)
    print("Path files: app/build/output/patch")

@click.command()
@click.option('--apk', help='base apk file for multi categoryChannel')
@click.option('--channel_file', help='categoryChannel.txt for walle')
@click.option('--output', help='output directory for generated apks')
def walle(apk, channel_file, output):
    apk_file = check_file_exists(apk)
    apk_info = parse_apk_info(apk_file)
    dump_apk_info(apk_info)

    commands = ['java', '-jar', 'scripts/walle-cli-all.jar', 'batch']
    commands.append('-f')
    commands.append(check_file_exists(channel_file))
    commands.append(apk_file)
    commands.append(check_file_exists(output))
    print("Starting to run following command:\n\n{}\n\n".format(' '.join(commands)))
    subprocess.check_call(commands)

@click.command()
@click.option('--apk', help='base apk file for multi categoryChannel')
def dump(apk):
    apk_file = check_file_exists(apk)
    dump_apk_info(parse_apk_info(apk_file))

cli.add_command(patch)
cli.add_command(walle)
cli.add_command(dump)

if __name__ == '__main__':
    cli()