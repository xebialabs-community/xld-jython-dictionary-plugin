#
# Copyright 2019 XEBIALABS
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#

from java.text import SimpleDateFormat
from java.util import Date
from java.lang import System


def get_property(ci, property_name, default_value):
    if ci is None:
        return default_value

    if property_name == "id":
        return ci.getId()
    elif property_name == "name":
        return ci.getId().split('/')[-1]
    else:
        return ci.getProperty(property_name)

formatter= SimpleDateFormat("yyyy-MM-dd 'at' HH:mm");
date = Date(System.currentTimeMillis());

values = {'application':get_property(context.getApplication(),"name","APP_EMPTY"),
        'version':get_property(context.getApplicationVersion(),"name","VERSION_EMPTY"),
        'environment':get_property(context.getEnvironment(),"name","ENV_EMPTY"),
        'container':get_property(context.getContainer(),"name","CONTAINER_EMPTY"),
        'now': formatter.format(date)}

if logger.isDebugEnabled():
    logger.debug("dict values {0}: {1}".format(dictionary_id, values))

entries.setValues(values)

