module MyFilters
	def expand_urls(input, url='')
		url ||= '/'
		input.gsub /(\s+(href|src)\s*=\s*["|']{1})(\/[^\"'>]*)/ do
			$1+url+$3
		end
	end

	def prepend_if(input, char)
		if input[0] != char[0]
			char + input
		else
			input
		end
	end

	def trim_last(input)
		input[0..-2]
	end
end

Liquid::Template.register_filter MyFilters
