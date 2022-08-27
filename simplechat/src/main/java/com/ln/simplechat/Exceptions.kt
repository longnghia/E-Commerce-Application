package com.ln.simplechat

class ChannelNotFoundException(channel: String) : Exception("Channel $channel Not Found")
